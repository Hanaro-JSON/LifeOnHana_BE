package com.example.lifeonhana.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StatisticsResponseDTO(
	@JsonProperty("yearMonth") String yearMonth,
	@JsonProperty("totalExpense") Integer totalExpense,
	@JsonProperty("totalInterest") Integer totalInterest,
	@JsonProperty("expenseCategories") List<CategoryStatResponseDTO> expenseCategories
) {}
