package com.example.lifeonhana.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.lifeonhana.entity.LumpSum;

import io.swagger.v3.oas.annotations.media.Schema;

public record LumpSumResponseDTO(
	@Schema(description = "목돈 신청 id")
	Long lumpSumId,
	@Schema(description = "계좌 잔액")
	BigDecimal balance,
	@Schema(description = "목돈 신청 날짜")
	LocalDateTime requestDate
){
	public static LumpSumResponseDTO fromEntity(LumpSum lumpSum, BigDecimal balance) {
		return new LumpSumResponseDTO(
			lumpSum.getLumpSumId(),
			balance,
			lumpSum.getRequestDate()
		);
	}
}
