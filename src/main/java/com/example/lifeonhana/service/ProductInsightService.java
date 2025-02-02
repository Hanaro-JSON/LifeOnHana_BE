package com.example.lifeonhana.service;

import com.example.lifeonhana.dto.request.ProductInsightRequest;
import com.example.lifeonhana.dto.response.ProductInsightResponse;
import com.example.lifeonhana.entity.Article;
import com.example.lifeonhana.entity.Mydata;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.repository.ArticleRepository;
import com.example.lifeonhana.repository.HistoryRepository;
import com.example.lifeonhana.repository.ProductRepository;
import com.example.lifeonhana.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ProductInsightService {

	private static final String FLASK_URL = "https://lifeonhana-ai.topician.com/effect";
	private final RestTemplate restTemplate;
	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	private final ProductRepository productRepository;
	private final HistoryRepository historyRepository;

	public ProductInsightResponse getProductInsight(ProductInsightRequest request, String authId) {
		// 인증 확인
		if (authId == null) {
			throw new BaseException(ErrorCode.AUTH_REQUIRED);
		}
		
		// 유효성 검사
		if (request.productId() == null) {
			throw new BaseException(ErrorCode.INVALID_INPUT, "productId는 필수값입니다");
		}
		
		User user = getUser(authId);
		Article article = getArticle(request.articleId());
		Product product = getProduct(request.productId());
		Map<String, Object> userData = prepareUserData(user);

		Map<String, Object> flaskRequest = prepareFlaskRequest(product, article, userData);
		Map<String, Object> flaskResponse = callFlaskAPI(flaskRequest);

		String analysisResult = (String) flaskResponse.getOrDefault("analysisResult", "No analysis result provided");
		String productLink = (String) flaskResponse.getOrDefault("productLink", "N/A");

		boolean isLiked = product.getProductLikes().stream()
			.anyMatch(productLike -> productLike.getUser().getUserId().equals(user.getUserId()));

		return new ProductInsightResponse(
			analysisResult,
			productLink,
			product.getName(),
			isLiked
		);
	}

	private User getUser(String authId) {
		return userRepository.findByAuthId(authId)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, authId));
	}

	private Article getArticle(Long articleId) {
		return articleRepository.findById(articleId)
			.orElseThrow(() -> new BaseException(ErrorCode.ARTICLE_NOT_FOUND, articleId));
	}

	private Product getProduct(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new BaseException(ErrorCode.PRODUCT_NOT_FOUND, productId));
	}

	private Map<String, Object> prepareUserData(User user) {
		Mydata mydata = Optional.ofNullable(user.getMydata())
			.orElseThrow(() -> new BaseException(ErrorCode.MYDATA_NOT_FOUND, user.getUserId()));

		return Map.of(
			"total_asset", Optional.ofNullable(mydata.getTotalAsset()).orElse(BigDecimal.ZERO),
			"deposit_amount", Optional.ofNullable(mydata.getDepositAmount()).orElse(BigDecimal.ZERO),
			"savings_amount", Optional.ofNullable(mydata.getSavingsAmount()).orElse(BigDecimal.ZERO),
			"loan_amount", Optional.ofNullable(mydata.getLoanAmount()).orElse(BigDecimal.ZERO),
			"recent_histories", getRecentHistories(user.getUserId())
		);
	}

	private Map<String, Object> prepareFlaskRequest(Product product, Article article, Map<String, Object> userData) {
		return Map.of(
			"product", Map.of(
				"name", Optional.ofNullable(product.getName()).orElse("N/A"),
				"description", Optional.ofNullable(product.getDescription()).orElse("N/A"),
				"category", Optional.ofNullable(product.getCategory()).map(Enum::toString).orElse("UNKNOWN"),
				"basic_interest_rate", Optional.ofNullable(product.getBasicInterestRate()).orElse(BigDecimal.ZERO),
				"max_interest_rate", Optional.ofNullable(product.getMaxInterestRate()).orElse(BigDecimal.ZERO),
				"min_amount", Optional.ofNullable(product.getMinAmount()).orElse(BigDecimal.ZERO),
				"max_amount", Optional.ofNullable(product.getMaxAmount()).orElse(BigDecimal.ZERO),
				"link", Optional.ofNullable(product.getLink()).orElse("N/A")
			),
			"articleShorts", Optional.ofNullable(article.getShorts()).orElse(""),
			"userData", userData
		);
	}

	private List<Map<String, Object>> getRecentHistories(Long userId) {
		return historyRepository.findTop5ByUser_UserIdOrderByHistoryDatetimeDesc(userId).stream()
			.map(history -> {
				Map<String, Object> historyMap = new HashMap<>();
				historyMap.put("category", Optional.ofNullable(history.getCategory()).map(Enum::toString).orElse("UNKNOWN"));
				historyMap.put("description", Optional.ofNullable(history.getDescription()).orElse("N/A"));
				historyMap.put("amount", Optional.ofNullable(history.getAmount()).orElse(BigDecimal.ZERO));
				return historyMap;
			})
			.collect(Collectors.toList());
	}

	private Map<String, Object> callFlaskAPI(Map<String, Object> flaskRequest) {
		try {
			Map<String, Object> response = restTemplate.postForObject(FLASK_URL, flaskRequest, Map.class);
			if (response == null || !response.containsKey("analysisResult")) {
				throw new IllegalArgumentException("Flask 응답이 올바르지 않습니다.");
			}
			return response;
		} catch (Exception e) {
			throw new BaseException(ErrorCode.FLASK_API_ERROR, e);
		}
	}
}
