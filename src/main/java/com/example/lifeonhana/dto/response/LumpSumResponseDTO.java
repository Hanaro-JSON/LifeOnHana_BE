package com.example.lifeonhana.dto.response;

import java.time.LocalDateTime;

import com.example.lifeonhana.entity.LumpSum;

public record LumpSumResponseDTO(
	Long lumpSumId,
	LocalDateTime requestDate
){
	public static LumpSumResponseDTO fromEntity(LumpSum lumpSum){
		return new LumpSumResponseDTO(
			lumpSum.getLumpSumId(),
			lumpSum.getRequestDate()
		);
	}
}
