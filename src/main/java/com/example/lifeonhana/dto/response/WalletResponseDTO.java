package com.example.lifeonhana.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record WalletResponseDTO(
	@Schema(description = "하나지갑 ID")
	long walletId,
	long walletAmount,
	String paymentDay,
	String startDate,
	String endDate
) {
}
