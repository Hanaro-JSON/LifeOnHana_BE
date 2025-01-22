package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;

import com.example.lifeonhana.entity.Product;

public record SavingProductResponseDTO(
	Long productId,
	String name,
	String description,
	String link,
	boolean isLike,
	SavingsInfo savingsInfo
) {
	public record SavingsInfo(
		BigDecimal basicInterestRate,
		BigDecimal maxInterestRate) {
	}

	public static SavingProductResponseDTO fromEntity(Product product, boolean isLike) {
		return new SavingProductResponseDTO(
			product.getProductId(),
			product.getName(),
			product.getDescription(),
			product.getLink(),
			isLike,
			new SavingProductResponseDTO.SavingsInfo(
				product.getBasicInterestRate(),
				product.getMaxInterestRate()
			)
		);
	}
}
