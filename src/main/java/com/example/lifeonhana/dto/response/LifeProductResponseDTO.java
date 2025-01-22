package com.example.lifeonhana.dto.response;

import com.example.lifeonhana.entity.Product;

public record LifeProductResponseDTO(
	Long productId,
	String name,
	String description,
	String link,
	boolean isLike
) {
	public static LifeProductResponseDTO fromEntity(Product product, boolean isLike) {
		return new LifeProductResponseDTO(
			product.getProductId(),
			product.getName(),
			product.getDescription(),
			product.getLink(),
			isLike
		);
	}
}
