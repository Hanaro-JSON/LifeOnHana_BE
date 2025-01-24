package com.example.lifeonhana.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record WalletRequestDTO(
	@Schema(description = "하나 지갑 금액")
	long walletAmount,
	String paymentDay,
	String startDate,
	String endDate
) {
}
