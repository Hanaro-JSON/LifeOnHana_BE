package com.example.lifeonhana.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountListResponseDTO(
	@JsonProperty("mainAccount") AccountResponseDTO mainAccount,
	@JsonProperty("otherAccounts") List<AccountResponseDTO> otherAccounts
) {}
