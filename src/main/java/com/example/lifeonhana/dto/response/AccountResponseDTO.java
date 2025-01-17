package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountResponseDTO(
	@JsonProperty("bank") String bank,
	@JsonProperty("accountNumber") String accountNumber,
	@JsonProperty("accountName") String accountName,
	@JsonProperty("balance") BigDecimal balance
) {}
