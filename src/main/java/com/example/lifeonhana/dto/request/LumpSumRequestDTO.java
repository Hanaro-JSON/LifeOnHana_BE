package com.example.lifeonhana.dto.request;

import java.math.BigDecimal;

import com.example.lifeonhana.entity.LumpSum;

public record LumpSumRequestDTO(
	BigDecimal amount,
	LumpSum.Source source,
	LumpSum.Reason reason,
	String reasonDetail,
	Long accountId
) {
}
