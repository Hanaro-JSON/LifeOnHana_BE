package com.example.lifeonhana.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record HistoryResponseDTO(
	@JsonProperty("yearMonth") String yearMonth,
	@JsonProperty("totalIncome") BigDecimal totalIncome,
	@JsonProperty("totalExpense") BigDecimal totalExpense,
	@JsonProperty("histories") List<HistoryDetailResponseDTO> histories,
	@JsonProperty("hasNext") boolean hasNext
) {}
