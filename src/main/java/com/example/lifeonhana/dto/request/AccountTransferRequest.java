package com.example.lifeonhana.dto.request;

import java.math.BigDecimal;

public record AccountTransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
}
