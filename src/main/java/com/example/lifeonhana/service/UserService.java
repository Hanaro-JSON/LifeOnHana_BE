package com.example.lifeonhana.service;

import com.example.lifeonhana.dto.response.UserResponseDTO;
import com.example.lifeonhana.dto.response.MyDataResponseDTO;
import com.example.lifeonhana.entity.*;
import com.example.lifeonhana.repository.ArticleLikeRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.HistoryRepository;
import com.example.lifeonhana.dto.response.UserNicknameResponseDTO;
import com.example.lifeonhana.entity.enums.ArticleCategory;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.example.lifeonhana.repository.ArticleRepository;
import org.springframework.data.redis.core.RedisTemplate;

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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Transactional(readOnly = true)
    public UserResponseDTO getUserInfo(String authId) {
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, authId));
            
        return new UserResponseDTO(
            user.getUserId(),
            user.getName(),
            user.getAuthId(),
            user.getBirthday(),
            user.getIsFirst()
        );
    }

    @Transactional(readOnly = true)
    public MyDataResponseDTO getMyData(String authId) {
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, authId));
            
        Mydata mydata = user.getMydata();
        if (mydata == null) {
            throw new BaseException(ErrorCode.MYDATA_NOT_FOUND, user.getUserId());
        }

        Account salaryAccount = mydata.getAccounts().stream()
            .filter(account -> account.getServiceAccount() == Account.ServiceAccount.SALARY)
            .findFirst()
            .orElseThrow(() -> new BaseException(ErrorCode.SALARY_ACCOUNT_NOT_FOUND));

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
        User user = userRepository.findByAuthId(authId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, authId));

        String userLikesKey = "user:" + user.getUserId() + ":likes";
        
        // 2. Redis에서 사용자의 좋아요 정보 조회
        Map<Object, Object> likedArticlesMap = redisTemplate.opsForHash().entries(userLikesKey);
        
        if (likedArticlesMap.isEmpty()) {
            // Redis에 데이터가 없으면 DB에서 조회하고 캐싱
            List<Article> articles = articleRepository.findAll();
            for (Article article : articles) {
                Boolean isLiked = articleRepository.isUserLikedArticle(article.getArticleId(), user.getUserId());
                if (isLiked) {
                    redisTemplate.opsForHash().put(userLikesKey, article.getArticleId().toString(), true);
                }
            }
            
            // DB에서 가장 많이 좋아요한 카테고리 조회
            Optional<String> categoryStrOpt = articleLikeRepository.findMostLikedCategory(authId);
            if (categoryStrOpt.isEmpty()) {
                throw new BaseException(ErrorCode.NO_LIKED_ARTICLES);
            }
            
            ArticleCategory topCategory = ArticleCategory.valueOf(categoryStrOpt.get());
            String nickname = topCategory.generateNickname(user.getName());
            return UserNicknameResponseDTO.builder()
                .nickname(nickname)
                .category(topCategory)
                .build();
        } 

        // Redis 데이터로 카테고리 계산
        List<Long> likedArticleIds = likedArticlesMap.entrySet().stream()
            .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
            .map(entry -> Long.parseLong(entry.getKey().toString()))
            .collect(Collectors.toList());

        if (likedArticleIds.isEmpty()) {
            throw new BaseException(ErrorCode.NO_LIKED_ARTICLES);
        }

        Map<Article.Category, Long> categoryCount = articleRepository.findAllByArticleIdIn(likedArticleIds)
            .stream()
            .collect(Collectors.groupingBy(
                Article::getCategory,
                Collectors.counting()
            ));

        Article.Category dbTopCategory = categoryCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseGet(() -> {
                return null;  // 여기도 빈 응답 처리
            });
            
        if (dbTopCategory == null) {
            throw new BaseException(ErrorCode.NO_LIKED_ARTICLES);
        }

        ArticleCategory topCategory = ArticleCategory.valueOf(dbTopCategory.name());
        String nickname = topCategory.generateNickname(user.getName());

        return UserNicknameResponseDTO.builder()
            .nickname(nickname)
            .category(topCategory)
            .build();
    }
} 