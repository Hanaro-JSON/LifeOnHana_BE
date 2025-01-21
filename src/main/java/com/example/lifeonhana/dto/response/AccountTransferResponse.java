package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;

public record AccountTransferResponse(BigDecimal amount,AccountResponseDTO fromAccount,AccountResponseDTO toAccount) {
}
