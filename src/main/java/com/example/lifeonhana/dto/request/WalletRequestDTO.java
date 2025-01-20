package com.example.lifeonhana.dto.request;

public record WalletRequestDTO(
	long walletAmount,
	String paymentDay,
	String startDate,
	String endDate
) {
}
