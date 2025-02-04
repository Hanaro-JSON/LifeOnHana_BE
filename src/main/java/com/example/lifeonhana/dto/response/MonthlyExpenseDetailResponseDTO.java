package com.example.lifeonhana.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonthlyExpenseDetailResponseDTO(
	@JsonProperty("month") String month,
	@JsonProperty("totalExpense") Integer totalExpense
) {}
