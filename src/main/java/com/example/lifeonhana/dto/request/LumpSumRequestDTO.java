package com.example.lifeonhana.dto.request;

import java.math.BigDecimal;

import com.example.lifeonhana.entity.LumpSum;

import io.swagger.v3.oas.annotations.media.Schema;

public record LumpSumRequestDTO(
	@Schema(description = "목돈 금액")
	BigDecimal amount,
	@Schema(description = "목돈 출처")
	LumpSum.Source source,
	@Schema(description = "목돈 사용 이유")
	LumpSum.Reason reason,
	@Schema(description = "상세 이유")
	String reasonDetail,
	@Schema(description = "계좌 ID")
	Long accountId
) {
}
