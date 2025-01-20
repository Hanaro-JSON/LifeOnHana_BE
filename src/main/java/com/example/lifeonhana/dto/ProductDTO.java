package com.example.lifeonhana.dto;

import java.math.BigDecimal;

import com.example.lifeonhana.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	Long productId;
	String name;
	String description;
	String category;
	BigDecimal minAmount;
	BigDecimal maxAmount;
	BigDecimal basicInterestRate;
	BigDecimal maxInterestRate;
	Integer maxPeriod;
	Long minCreditScore;

	public static ProductDTO fromEntity(Product product) {
		return ProductDTO.builder()
			.productId(product.getProductId())
			.name(product.getName())
			.description(product.getDescription())
			.category(product.getCategory().name())
			.minAmount(product.getMinAmount())
			.maxAmount(product.getMaxAmount())
			.basicInterestRate(product.getBasicInterestRate())
			.maxInterestRate(product.getMaxInterestRate())
			.maxPeriod(product.getMaxPeriod())
			.minAmount(product.getMinAmount())
			.build();
	}
}
