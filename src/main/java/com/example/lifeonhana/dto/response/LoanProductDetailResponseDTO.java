package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;

import com.example.lifeonhana.entity.Product;

public record LoanProductDetailResponseDTO(
	Long productId,
	String name,
	String description,
	String feature,
	String target,
	String link,
	boolean isLiked,
	LoanInfo loanInfo
) {
	public record LoanInfo(
		BigDecimal minAmount,
		BigDecimal maxAmount,
		BigDecimal basicInterestRate,
		BigDecimal maxInterestRate,
		Integer minPeriod,
		Integer maxPeriod,
		Long minCreditScore
	) {
	}

	public static LoanProductDetailResponseDTO fromEntity(Product product, boolean isLiked) {
		return new LoanProductDetailResponseDTO(
			product.getProductId(),
			product.getName(),
			product.getDescription(),
			product.getFeature(),
			product.getTarget(),
			product.getLink(),
			isLiked,
			new LoanProductDetailResponseDTO.LoanInfo(
				product.getMinAmount(),
				product.getMaxAmount(),
				product.getBasicInterestRate(),
				product.getMaxInterestRate(),
				product.getMinPeriod(),
				product.getMaxPeriod(),
				product.getMinCreditScore()
			)
		);
	}
}
