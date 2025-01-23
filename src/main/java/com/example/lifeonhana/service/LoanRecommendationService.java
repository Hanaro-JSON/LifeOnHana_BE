package com.example.lifeonhana.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.lifeonhana.dto.response.LoanProductResponse;
import com.example.lifeonhana.entity.Mydata;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.LoanProductRepository;
import com.example.lifeonhana.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanRecommendationService {

	private static final String flaskUrl = "https://lifeonhana-ai.topician.com/recommend_loan_products";

	private final LoanProductRepository loanProductRepository;
	private final UserRepository userRepository;
	private final RestTemplate restTemplate;
	private static final Logger log = LoggerFactory.getLogger(LoanRecommendationService.class);

	private User getUser(String authId) {
		log.info("Fetching user with authId: {}", authId);
		return userRepository.findByAuthId(authId)
			.orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));
	}

	public List<LoanProductResponse> recommendLoanProducts(String reason, BigDecimal amount, String authId) {
		log.info("Starting loan product recommendation process");
		log.info("Reason: {}, Amount: {}, AuthId: {}", reason, amount, authId);

		// 사용자 조회
		User user = getUser(authId);
		log.info("Found user: {} (userId: {})", user.getName(), user.getUserId());

		// 사용자 마이데이터 확인
		Mydata mydata = user.getMydata();
		if (mydata == null) {
			log.error("Mydata not found for userId: {}", user.getUserId());
			throw new BadRequestException("사용자의 마이데이터 정보가 존재하지 않습니다.");
		}

		log.info("User Mydata: deposit_amount={}, loan_amount={}, real_estate_amount={}, total_asset={}",
			mydata.getDepositAmount(), mydata.getLoanAmount(), mydata.getRealEstateAmount(), mydata.getTotalAsset());

		// 대출 상품 조회
		log.info("Fetching loan products with category LOAN");
		List<Product> loanProducts = loanProductRepository.findByCategory(Product.Category.LOAN);
		log.info("Found {} loan products", loanProducts.size());

		if (loanProducts.isEmpty()) {
			log.error("No loan products found in the database");
			throw new NotFoundException("대출 상품이 존재하지 않습니다.");
		}

		// Flask 요청 데이터 구성
		Map<String, Object> request = Map.of(
			"reason", reason,
			"amount", amount,
			"userData", Map.of(
				"deposit_amount", mydata.getDepositAmount() != null ? mydata.getDepositAmount() : BigDecimal.ZERO,
				"loan_amount", mydata.getLoanAmount() != null ? mydata.getLoanAmount() : BigDecimal.ZERO,
				"real_estate_amount", mydata.getRealEstateAmount() != null ? mydata.getRealEstateAmount() : BigDecimal.ZERO,
				"total_asset", mydata.getTotalAsset() != null ? mydata.getTotalAsset() : BigDecimal.ZERO
			),
			"products", loanProducts.stream()
				.map(product -> Map.of(
					"id", product.getProductId(),
					"name", product.getName() != null ? product.getName() : "N/A",
					"description", product.getDescription() != null ? product.getDescription() : "N/A",
					"minAmount", product.getMinAmount() != null ? product.getMinAmount() : BigDecimal.ZERO,
					"maxAmount", product.getMaxAmount() != null ? product.getMaxAmount() : BigDecimal.ZERO
				))
				.toList()
		);

		log.info("Constructed Flask request: {}", request);

		try {
			// Flask API 호출
			log.info("Sending request to Flask");
			var response = restTemplate.postForEntity(flaskUrl, request, Map.class);
			log.info("Received response from Flask: {}", response);

			// Flask 응답 처리
			log.info("Received response from Flask: {}", response);
			if (response.getBody() == null) {
				log.error("Flask API response body is null");
				throw new BadRequestException("Flask API 응답에 데이터가 없습니다.");
			}
			if (response.getBody().get("products") == null) {
				log.error("Flask API response does not contain 'products'");
				throw new BadRequestException("Flask API 응답에 'products'가 없습니다.");
			}

			List<Map<String, Object>> recommendedProducts = (List<Map<String, Object>>) response.getBody().get("products");

			if (recommendedProducts.isEmpty()) {
				log.warn("No recommended loan products found in Flask response");
				throw new NotFoundException("추천된 대출 상품이 없습니다.");
			}

			log.info("Recommended products received: {}", recommendedProducts);

			// 추천 결과 생성
			return recommendedProducts.stream()
				.map(product -> {
					log.info("Mapping product: {}", product);
					Long productId = Optional.ofNullable(product.get("id"))
						.map(Object::toString)
						.map(Long::valueOf)
						.orElseThrow(() -> new IllegalArgumentException("Product ID is missing"));

					String name = Optional.ofNullable(product.get("name"))
						.map(Object::toString)
						.orElse("N/A");

					String description = Optional.ofNullable(product.get("description"))
						.map(Object::toString)
						.orElse("N/A");

					BigDecimal minAmount = Optional.ofNullable(product.get("minAmount"))
						.map(value -> value == null ? null : new BigDecimal(value.toString()))
						.orElse(null);

					BigDecimal maxAmount = Optional.ofNullable(product.get("maxAmount"))
						.map(value -> value == null ? null : new BigDecimal(value.toString()))
						.orElse(null);

					Integer score = Optional.ofNullable(product.get("score"))
						.map(Object::toString)
						.map(Integer::valueOf)
						.orElse(0);

					return new LoanProductResponse(
						productId,
						name,
						description,
						minAmount,
						maxAmount,
						score
					);
				}).toList();

		} catch (HttpClientErrorException e) {
			log.error("HTTP error occurred while calling Flask: {}", e.getMessage(), e);
			throw new BadRequestException("Flask API 호출 중 오류가 발생했습니다.");
		} catch (Exception e) {
			log.error("Unexpected error occurred while calling Flask: {}", e.getMessage(), e);
			throw new RuntimeException("예상치 못한 오류가 발생했습니다.", e);
		}
	}
}
