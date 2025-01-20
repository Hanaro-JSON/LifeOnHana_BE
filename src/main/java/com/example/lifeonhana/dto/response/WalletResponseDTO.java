package com.example.lifeonhana.dto.response;

public record WalletResponseDTO(
	long walletId,
	long walletAmount,
	String paymentDay,
	String startDate,
	String endDate
) {
}
