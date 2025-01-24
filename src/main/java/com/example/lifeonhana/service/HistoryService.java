package com.example.lifeonhana.service;

import com.example.lifeonhana.dto.response.CategoryStatResponseDTO;
import com.example.lifeonhana.dto.response.HistoryDetailResponseDTO;
import com.example.lifeonhana.dto.response.HistoryResponseDTO;
import com.example.lifeonhana.dto.response.MonthlyExpenseDetailResponseDTO;
import com.example.lifeonhana.dto.response.MonthlyExpenseResponseDTO;
import com.example.lifeonhana.dto.response.StatisticsResponseDTO;
import com.example.lifeonhana.entity.History;
import com.example.lifeonhana.entity.History.Category;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.repository.HistoryRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {
	private final HistoryRepository historyRepository;
	private final UserRepository userRepository;
	private final WalletRepository walletRepository;

	// 공통 유틸리티 메서드
	private User getUser(String authId) {
		return userRepository.findByAuthId(authId)
			.orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));
	}

	private YearMonth parseYearMonth(String yearMonth) {
		try {
			return YearMonth.parse(yearMonth); // ISO 형식 (YYYY-MM)은 기본 파싱 지원
		} catch (DateTimeParseException e) {
			throw new BadRequestException("올바른 년월 형식이 아닙니다. (YYYY-MM)");
		}
	}

	private String convertToISOFormat(String yyyymm) {
		return yyyymm.substring(0, 4) + "-" + yyyymm.substring(4);
	}

	private LocalDateTime[] getDateRange(YearMonth ym) {
		LocalDateTime startDate = ym.atDay(1).atStartOfDay();
		LocalDateTime endDate = ym.atEndOfMonth().atTime(23, 59, 59);
		return new LocalDateTime[]{startDate, endDate};
	}

	private Integer calculatePercentage(BigDecimal amount, BigDecimal total) {
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}
		return amount.multiply(new BigDecimal("100"))
			.divide(total, 0, RoundingMode.HALF_UP)
			.intValue();
	}

	// 1. 입출금 내역 조회
	@Transactional(readOnly = true)
	public HistoryResponseDTO getHistories(String yearMonth, String authId, int page, int size) {
		User user = getUser(authId);
		YearMonth ym = parseYearMonth(yearMonth);
		LocalDateTime[] dateRange = getDateRange(ym);

		Slice<History> historiesSlice = historyRepository.findAllByUserAndYearMonth(
			user, dateRange[0], dateRange[1], PageRequest.of(page - 1, size));

		BigDecimal totalIncome = historyRepository.calculateTotalIncome(user, dateRange[0], dateRange[1]);
		BigDecimal totalExpense = historyRepository.calculateTotalExpense(user, dateRange[0], dateRange[1]);

		List<HistoryDetailResponseDTO> historyDTOs = historiesSlice.getContent().stream()
			.map(this::convertToHistoryDTO)
			.collect(Collectors.toList());

		return new HistoryResponseDTO(
			yearMonth,
			totalIncome,
			totalExpense,
			historyDTOs,
			historiesSlice.hasNext()
		);
	}

	private HistoryDetailResponseDTO convertToHistoryDTO(History history) {
		return new HistoryDetailResponseDTO(
			history.getHistoryId(),
			history.getCategory(),
			history.getAmount(),
			history.getDescription(),
			history.getHistoryDatetime(),
			history.getIsFixed(),
			history.getIsExpense()
		);
	}

	// 2. 월별 지출 내역 조회
	@Transactional(readOnly = true)
	public MonthlyExpenseResponseDTO getMonthlyExpenses(String authId) {
		User user = getUser(authId);
		YearMonth currentMonth = YearMonth.now();
		LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);
		LocalDateTime startDate = currentMonth.minusMonths(4).atDay(1).atStartOfDay();

		List<Object[]> monthlyExpensesRaw = historyRepository.findMonthlyExpenses(user, startDate, endDate);

		Map<String, Integer> expensesByMonth = monthlyExpensesRaw.stream()
			.collect(Collectors.toMap(
				row -> (String) row[0],  // 이미 YYYY-MM 형식으로 반환됨
				row -> (Integer) row[1]
			));

		List<MonthlyExpenseDetailResponseDTO> monthlyExpenses = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			YearMonth month = currentMonth.minusMonths(i);
			String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

			Integer expense = expensesByMonth.getOrDefault(monthStr, 0);
			monthlyExpenses.add(new MonthlyExpenseDetailResponseDTO(
				monthStr,
				expense
			));
		}

		return new MonthlyExpenseResponseDTO(
			historyRepository.calculateAverageMonthlyExpense(user, startDate, endDate),
			walletRepository.findCurrentBalance(user),
			monthlyExpenses
		);
	}

	// 3. 거래 통계 조회
	@Transactional(readOnly = true)
	public StatisticsResponseDTO getStatistics(String yearMonth, String authId) {
		User user = getUser(authId);
		YearMonth ym = parseYearMonth(yearMonth);
		LocalDateTime[] dateRange = getDateRange(ym);

		BigDecimal totalExpense = historyRepository.calculateTotalExpense(
			user, dateRange[0], dateRange[1]);
		BigDecimal totalInterest = historyRepository.calculateTotalInterest(
			user, dateRange[0], dateRange[1]);

		Map<Category, BigDecimal> categoryAmounts = new HashMap<>();
		historyRepository.findExpenseStatsByCategory(user, dateRange[0], dateRange[1])
			.forEach(stat -> categoryAmounts.put(stat.getCategory(), stat.getAmount()));

		List<CategoryStatResponseDTO> expenseCategories = Arrays.stream(Category.values())
			.filter(category -> category != Category.INTEREST)
			.map(category -> {
				BigDecimal amount = categoryAmounts.getOrDefault(category, BigDecimal.ZERO);
				return new CategoryStatResponseDTO(
					category,
					amount,
					calculatePercentage(amount, totalExpense)
				);
			})
			.collect(Collectors.toList());

		String formattedYearMonth = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));

		return new StatisticsResponseDTO(
			yearMonth,
			totalExpense.intValue(),
			totalInterest.intValue(),
			expenseCategories
		);
	}
}
