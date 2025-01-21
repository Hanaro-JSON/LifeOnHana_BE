package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;

import com.example.lifeonhana.entity.Product;

public record SimpleProductResponseDTO(
	Long productId,
	String name,
	String description,
	BigDecimal maxAmount,
	String category) {

	public static SimpleProductResponseDTO fromEntity(Product product) {
		return new SimpleProductResponseDTO(
			product.getProductId(),
			product.getName(),
			product.getDescription(),
			product.getMaxAmount(),
			product.getCategory().name()
		);
	}
}
