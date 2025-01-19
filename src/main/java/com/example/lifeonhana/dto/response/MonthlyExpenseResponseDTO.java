package com.example.lifeonhana.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MonthlyExpenseResponseDTO(
	@JsonProperty("averageExpense") Integer averageExpense,
	@JsonProperty("currentBalance") Integer currentBalance,
	@JsonProperty("monthlyExpenses") List<MonthlyExpenseDetailResponseDTO> monthlyExpenses
) {}
