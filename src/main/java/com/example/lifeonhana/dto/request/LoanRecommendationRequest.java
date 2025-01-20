package com.example.lifeonhana.dto.request;

import java.math.BigDecimal;

public record LoanRecommendationRequest(String reason, BigDecimal amount) {
}
