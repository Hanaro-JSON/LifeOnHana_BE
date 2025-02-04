package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;

public record LoanProductResponse(
	Long productId,
	String name,
	String description,
	BigDecimal minAmount,
	BigDecimal maxAmount,
	int score
) {
}
