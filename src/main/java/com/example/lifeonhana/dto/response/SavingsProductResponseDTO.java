package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;


public record SavingsProductResponseDTO(
	Long productId,
	String name,
	String description,
	String link,
	SavingsInfo savingsInfo
) {
	public record SavingsInfo(
		BigDecimal basicInterestRate,
		BigDecimal maxInterestRate) {
	}
}
