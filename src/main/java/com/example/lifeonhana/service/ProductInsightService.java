package com.example.lifeonhana.service;

import com.example.lifeonhana.dto.request.ProductInsightRequest;
import com.example.lifeonhana.dto.response.ProductInsightResponse;
import com.example.lifeonhana.entity.*;
import com.example.lifeonhana.global.exception.UnauthorizedException;
import com.example.lifeonhana.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ProductInsightService {

	private static final String FLASK_URL = "https://lifeonhana-ai.topician.com/effect";
	private final RestTemplate restTemplate;
	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	private final ProductRepository productRepository;
	private final HistoryRepository historyRepository;
	private final ProductLikeRepository productLikeRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	public ProductInsightResponse getProductInsight(ProductInsightRequest request, String authId) {
		User user = getUser(authId);
		Article article = getArticle(request.articleId());
		Product product = getProduct(request.productId());
		Map<String, Object> userData = prepareUserData(user);

		Map<String, Object> flaskRequest = prepareFlaskRequest(product, article, userData);
		Map<String, Object> flaskResponse = callFlaskAPI(flaskRequest);

		String analysisResult = (String) flaskResponse.getOrDefault("analysisResult", "No analysis result provided");
		String productLink = (String) flaskResponse.getOrDefault("productLink", "N/A");

		String userLikesKey = "user:" + user.getUserId() + ":productLikes";
		Map<Object, Object> likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);

		if (likedProductsMap.isEmpty()) {
			List<ProductLike> dbLikes = productLikeRepository.findByUserAndIsLikeTrue(user);
			for (ProductLike like : dbLikes) {
				redisTemplate.opsForHash().put(userLikesKey,
					like.getProduct().getProductId().toString(), true);
			}
			likedProductsMap = redisTemplate.opsForHash().entries(userLikesKey);
		}

		Boolean redisProduct = (Boolean) redisTemplate.opsForHash().get(userLikesKey, product.getProductId().toString());
		boolean isLiked = Boolean.TRUE.equals(redisProduct);

		return new ProductInsightResponse(
			analysisResult,
			productLink,
			product.getName(),
			isLiked
		);
	}

	private User getUser(String authId) {
		return userRepository.findByAuthId(authId)
			.orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다. authId: " + authId));
	}

	private Article getArticle(Long articleId) {
		return articleRepository.findById(articleId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기사입니다. articleId: " + articleId));
	}

	private Product getProduct(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. productId: " + productId));
	}

	private Map<String, Object> prepareUserData(User user) {
		Mydata mydata = Optional.ofNullable(user.getMydata())
			.orElseThrow(() -> new IllegalArgumentException("사용자의 자산 정보가 없습니다. userId: " + user.getUserId()));

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
			throw new RuntimeException("Flask API 호출 중 오류가 발생했습니다.", e);
		}
	}
}
