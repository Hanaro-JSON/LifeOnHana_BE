package com.example.lifeonhana.dto.response;

import com.example.lifeonhana.entity.History.Category;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record CategoryStatResponseDTO(
	@JsonProperty("category") Category category,
	@JsonProperty("amount") BigDecimal amount,
	@JsonProperty("percentage") Integer percentage
) {}
