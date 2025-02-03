package com.example.lifeonhana.service;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.lifeonhana.dto.response.LoanProductResponse;
import com.example.lifeonhana.entity.Mydata;
import com.example.lifeonhana.entity.Product;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.example.lifeonhana.repository.LoanProductRepository;
import com.example.lifeonhana.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
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
			.orElseThrow(() -> new BaseException(ErrorCode.AUTH_REQUIRED));
	}

	public List<LoanProductResponse> recommendLoanProducts(String reason, BigDecimal amount, String authId) {
		if (reason == null || reason.isBlank()) {
			throw new BaseException(ErrorCode.LOAN_REASON_REQUIRED);
		}
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BaseException(ErrorCode.LOAN_AMOUNT_INVALID);
		}

		User user = getUser(authId);

		Mydata mydata = user.getMydata();
		if (mydata == null) {
			throw new BaseException(ErrorCode.MYDATA_NOT_FOUND);
		}

		log.info("User Mydata: deposit_amount={}, loan_amount={}, real_estate_amount={}, total_asset={}",
			mydata.getDepositAmount(), mydata.getLoanAmount(), mydata.getRealEstateAmount(), mydata.getTotalAsset());

		log.info("Fetching loan products with category LOAN");
		List<Product> loanProducts = loanProductRepository.findByCategory(Product.Category.LOAN);
		if (loanProducts.isEmpty()) {
			throw new BaseException(ErrorCode.LOAN_PRODUCTS_NOT_FOUND);
		}
		log.info("Found {} loan products", loanProducts.size());

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
			log.info("Sending request to Flask");
			var response = restTemplate.postForEntity(flaskUrl, request, Map.class);
			log.info("Received response from Flask: {}", response);

			log.info("Received response from Flask: {}", response);
			if (response.getBody() == null) {
				throw new BaseException(ErrorCode.FLASK_RESPONSE_EMPTY);
			}
			if (response.getBody().get("products") == null) {
				throw new BaseException(ErrorCode.FLASK_RESPONSE_INVALID);
			}

			List<Map<String, Object>> recommendedProducts = (List<Map<String, Object>>) response.getBody().get("products");

			if (recommendedProducts.isEmpty()) {
				throw new BaseException(ErrorCode.NO_RECOMMENDED_PRODUCTS);
			}

			log.info("Recommended products received: {}", recommendedProducts);

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
			throw new BaseException(ErrorCode.FLASK_API_ERROR, e);
		} catch (Exception e) {
			throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}
}
