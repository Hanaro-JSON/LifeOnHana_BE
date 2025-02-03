package com.example.lifeonhana.controller;

import com.example.lifeonhana.entity.History;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
import com.example.lifeonhana.repository.HistoryRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.WalletRepository;
import com.example.lifeonhana.service.JwtService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HistoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Autowired
	private WalletRepository walletRepository;

	@Autowired
	private JwtService jwtService;

	private User testUser;
	private String authToken;
	private String currentYearMonth;

	@BeforeEach
	void setUp() {
		testUser = createTestUser();
		createTestWallet();
		authToken = "Bearer " + jwtService.generateAccessToken(testUser.getAuthId(), testUser.getUserId());
		currentYearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
		createTestHistories();
	}

	private User createTestUser() {
		User user = new User();
		user.setAuthId("test@example.com");
		user.setName("Test User");
		user.setBirthday("19900101");
		user.setIsFirst(true);
		return userRepository.save(user);
	}

	private void createTestWallet() {
		Wallet wallet = new Wallet();
		wallet.setUser(testUser);
		wallet.setWalletAmount(1000000L);
		wallet.setPaymentDay(Wallet.PaymentDay.DAY_1);
		wallet.setStartDate(LocalDateTime.now().withDayOfMonth(1));
		wallet.setEndDate(LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusSeconds(1));
		walletRepository.save(wallet);
	}

	private void createTestHistories() {
		LocalDateTime now = LocalDateTime.now();

		// 현재 월 데이터
		Stream.of(
			new HistoryTestData(History.Category.FOOD, "50000", "점심", true),
			new HistoryTestData(History.Category.HOBBY, "100000", "영화", true),
			new HistoryTestData(History.Category.DEPOSIT, "3000000", "급여", false),
			new HistoryTestData(History.Category.INTEREST, "5000", "이자수입", false),
			new HistoryTestData(History.Category.FIXED_EXPENSE, "500000", "월세", true, true)
		).forEach(data -> createHistory(data, now));

		// 지난 달 데이터
		LocalDateTime lastMonth = now.minusMonths(1);
		Stream.of(
			new HistoryTestData(History.Category.FOOD, "45000", "저녁", true),
			new HistoryTestData(History.Category.HOBBY, "30000", "도서", true),
			new HistoryTestData(History.Category.FIXED_EXPENSE, "500000", "월세", true, true)
		).forEach(data -> createHistory(data, lastMonth));
	}

	private void createHistory(HistoryTestData data, LocalDateTime dateTime) {
		History history = new History();
		history.setUser(testUser);
		history.setCategory(data.category);
		history.setAmount(new BigDecimal(data.amount));
		history.setDescription(data.description);
		history.setHistoryDatetime(dateTime);
		history.setIsExpense(data.isExpense);
		history.setIsFixed(data.isFixed);
		historyRepository.save(history);
	}

	@Nested
	@DisplayName("거래 내역 조회 API 테스트")
	class GetHistoriesTest {

		@Test
		@DisplayName("정상적인 거래 내역 조회 - 성공")
		void getHistories_Success() throws Exception {
			mockMvc.perform(get("/api/history")
					.header("Authorization", authToken)
					.param("yearMonth", currentYearMonth)
					.param("page", "1")
					.param("size", "20"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.status").value("OK"))
				.andExpect(jsonPath("$.message").value("거래 내역 조회 성공"))
				.andExpect(jsonPath("$.data.yearMonth").value(currentYearMonth))
				.andExpect(jsonPath("$.data.totalIncome").value(3005000))
				.andExpect(jsonPath("$.data.totalExpense").value(650000))
				.andExpect(jsonPath("$.data.histories").isArray())
				.andExpect(jsonPath("$.data.histories.length()").value(5))
				.andExpect(jsonPath("$.data.histories[0].isFixed").isBoolean());
		}

		@ParameterizedTest
		@CsvSource({
			"1, 2, 2",  // 첫 페이지, 2개씩
			"2, 2, 2",  // 두번째 페이지, 2개씩
			"3, 2, 1"   // 마지막 페이지, 남은 항목
		})
		@DisplayName("페이지네이션 테스트")
		void getHistories_Pagination(int page, int size, int expectedSize) throws Exception {
			mockMvc.perform(get("/api/history")
					.header("Authorization", authToken)
					.param("yearMonth", currentYearMonth)
					.param("page", String.valueOf(page))
					.param("size", String.valueOf(size)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.histories.length()").value(expectedSize));
		}

		@Test
		@DisplayName("잘못된 연월 형식으로 조회 시 실패")
		void getHistories_InvalidYearMonthFormat() throws Exception {
			mockMvc.perform(get("/api/history")
					.header("Authorization", authToken)
					.param("yearMonth", "202401")
					.param("page", "1")
					.param("size", "20"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("올바른 년월 형식이 아닙니다")));
		}
	}

	@Nested
	@DisplayName("월별 지출 내역 조회 API 테스트")
	class GetMonthlyExpensesTest {

		@Test
		@DisplayName("정상적인 월별 지출 내역 조회 - 성공")
		void getMonthlyExpenses_Success() throws Exception {
			mockMvc.perform(get("/api/history/monthly")
					.header("Authorization", authToken))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data.averageExpense").exists())
				.andExpect(jsonPath("$.data.currentBalance").value(1000000))
				.andExpect(jsonPath("$.data.monthlyExpenses").isArray())
				.andExpect(jsonPath("$.data.monthlyExpenses[0].month").value(currentYearMonth))
				.andExpect(jsonPath("$.data.monthlyExpenses[0].totalExpense").value(650000));
		}

		@Test
		@DisplayName("인증되지 않은 사용자 조회 시 실패")
		void getMonthlyExpenses_NoAuth() throws Exception {
			mockMvc.perform(get("/api/history/monthly"))
				.andDo(print())
				.andExpect(status().isUnauthorized())  // 401로 수정
				.andExpect(jsonPath("$.message").value("인증이 필요합니다."));
		}
	}

	@Nested
	@DisplayName("거래 통계 조회 API 테스트")
	class GetStatisticsTest {

		@Test
		@DisplayName("정상적인 거래 통계 조회 - 성공")
		void getStatistics_Success() throws Exception {
			mockMvc.perform(get("/api/history/statistics")
					.header("Authorization", authToken)
					.param("yearMonth", currentYearMonth))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.data.yearMonth").value(currentYearMonth))
				.andExpect(jsonPath("$.data.totalExpense").value(650000))
				.andExpect(jsonPath("$.data.totalInterest").value(5000))
				.andExpect(jsonPath("$.data.expenseCategories").isArray())
				.andExpect(jsonPath("$.data.expenseCategories[?(@.category=='FIXED_EXPENSE')].percentage").value(
					Matchers.notNullValue()))
				.andExpect(jsonPath("$.data.expenseCategories[?(@.category=='FOOD')].amount").value(50000));
		}

		@Test
		@DisplayName("미래 월 통계 조회 - 빈 결과 반환")
		void getStatistics_FutureMonth() throws Exception {
			String futureYearMonth = YearMonth.now()
				.plusMonths(1)
				.format(DateTimeFormatter.ofPattern("yyyy-MM"));

			mockMvc.perform(get("/api/history/statistics")
					.header("Authorization", authToken)
					.param("yearMonth", futureYearMonth))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalExpense").value(0))
				.andExpect(jsonPath("$.data.totalInterest").value(0))
				.andExpect(jsonPath("$.data.expenseCategories[*].amount")
					.value(everyItem(is(0))));
		}

		@Test
		@DisplayName("지난 월 통계 조회 - 성공")
		void getStatistics_PastMonth() throws Exception {
			String lastMonth = YearMonth.now()
				.minusMonths(1)
				.format(DateTimeFormatter.ofPattern("yyyy-MM"));

			mockMvc.perform(get("/api/history/statistics")
					.header("Authorization", authToken)
					.param("yearMonth", lastMonth))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalExpense").value(575000))
				.andExpect(jsonPath("$.data.expenseCategories[?(@.category=='FOOD')].amount")
					.value(45000));
		}
	}

	private static class HistoryTestData {
		History.Category category;
		String amount;
		String description;
		boolean isExpense;
		boolean isFixed;

		HistoryTestData(History.Category category, String amount, String description,
			boolean isExpense, boolean isFixed) {
			this.category = category;
			this.amount = amount;
			this.description = description;
			this.isExpense = isExpense;
			this.isFixed = isFixed;
		}

		HistoryTestData(History.Category category, String amount, String description,
			boolean isExpense) {
			this(category, amount, description, isExpense, false);
		}
	}
}
