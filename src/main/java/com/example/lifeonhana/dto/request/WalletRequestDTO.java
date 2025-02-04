package com.example.lifeonhana.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record WalletRequestDTO(
	@Schema(description = "하나지갑 금액")
	long walletAmount,
	@Schema(description = "하나지갑 월급 지급일")
	String paymentDay,
	@Schema(description = "시작일")
	String startDate,
	@Schema(description = "종료일")
	String endDate
) {
}
