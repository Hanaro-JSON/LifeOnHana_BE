package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SalaryAccountResponseDTO(
    @JsonProperty("accountId") Long accountId,
    @JsonProperty("balance") BigDecimal balance
) {} 