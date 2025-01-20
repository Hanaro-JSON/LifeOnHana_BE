package com.example.lifeonhana.service;

import com.example.lifeonhana.dto.response.UserResponseDTO;
import com.example.lifeonhana.dto.response.MyDataResponseDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Mydata;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.entity.History;
import com.example.lifeonhana.repository.HistoryRepository;
import com.example.lifeonhana.dto.response.UserNicknameResponseDTO;
import com.example.lifeonhana.entity.enums.ArticleCategory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional(readOnly = true)
    public UserResponseDTO getUserInfo(String authId) {
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
            
        return new UserResponseDTO(
            user.getUserId(),
            user.getName(),
            user.getAuthId(),
            user.getIsFirst()
        );
    }

    @Transactional(readOnly = true)
    public MyDataResponseDTO getMyData(String authId) {
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
            
        Mydata mydata = user.getMydata();
        if (mydata == null) {
            throw new NotFoundException("마이데이터 정보를 찾을 수 없습니다.");
        }

        Account salaryAccount = mydata.getAccounts().stream()
            .filter(account -> account.getServiceAccount() == Account.ServiceAccount.SALARY)
            .findFirst()
            .orElseThrow(() -> new NotFoundException("급여 계좌를 찾을 수 없습니다."));

        BigDecimal netAsset = mydata.getTotalAsset().subtract(mydata.getLoanAmount());
        
        return new MyDataResponseDTO(
            String.valueOf(mydata.getPensionStartYear()),
            mydata.getTotalAsset(),
            netAsset,
            mydata.getDepositAmount(),
            calculatePercentage(mydata.getDepositAmount(), mydata.getTotalAsset()),
            mydata.getSavingsAmount(),
            calculatePercentage(mydata.getSavingsAmount(), mydata.getTotalAsset()),
            mydata.getLoanAmount(),
            calculatePercentage(mydata.getLoanAmount(), mydata.getTotalAsset()),
            mydata.getStockAmount(),
            calculatePercentage(mydata.getStockAmount(), mydata.getTotalAsset()),
            mydata.getRealEstateAmount(),
            calculatePercentage(mydata.getRealEstateAmount(), mydata.getTotalAsset()),
            mydata.getLastUpdatedAt(),
            new MyDataResponseDTO.SalaryAccountDTO(
                salaryAccount.getAccountNumber(),
                salaryAccount.getBalance(),
                salaryAccount.getBank().name()
            ),
            calculateMonthlyFixedExpense(user)
        );
    }

    private Integer calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return 0;
        return amount.multiply(BigDecimal.valueOf(100))
            .divide(total, 0, RoundingMode.HALF_UP)
            .intValue();
    }

    private BigDecimal calculateMonthlyFixedExpense(User user) {
        List<History> fixedExpenses = historyRepository
            .findByUser_UserIdAndIsFixedAndIsExpenseOrderByHistoryDatetimeDesc(
                user.getUserId(), 
                true,   // isFixed
                true    // isExpense
            );


        if (fixedExpenses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 가장 최근 날짜 기준으로 3개월치 데이터만 사용
        LocalDateTime latestDate = fixedExpenses.get(0).getHistoryDatetime();
        LocalDateTime threeMonthsBefore = latestDate.minusMonths(3);

        Map<YearMonth, BigDecimal> monthlyTotals = fixedExpenses.stream()
            .filter(history -> !history.getHistoryDatetime().isBefore(threeMonthsBefore))
            .collect(Collectors.groupingBy(
                history -> YearMonth.from(history.getHistoryDatetime()),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    History::getAmount,
                    BigDecimal::add
                )
            ));


        if (monthlyTotals.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 월별 합계의 평균 계산
        BigDecimal totalAmount = monthlyTotals.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalAmount.divide(BigDecimal.valueOf(monthlyTotals.size()), 0, RoundingMode.HALF_UP);
    }

    public UserNicknameResponseDTO getUserNickname(String authId) {
        // 1. 사용자 정보 조회
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 사용자의 좋아요가 가장 많은 카테고리 조회
        String categoryStr = articleLikeRepository.findMostLikedCategory(authId)
            .orElseThrow(() -> new NotFoundException("좋아요 기록이 없습니다."));
        
        // 3. String을 Enum으로 변환
        ArticleCategory topCategory = ArticleCategory.valueOf(categoryStr);

        // 4. 칭호 생성
        String nickname = topCategory.generateNickname(user.getName());

        return UserNicknameResponseDTO.builder()
            .nickname(nickname)
            .category(topCategory)
            .build();
    }
} 