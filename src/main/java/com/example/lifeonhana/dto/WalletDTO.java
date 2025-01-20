package com.example.lifeonhana.dto;

public record WalletDTO (
	long walletId,
	long walletAmount,
	String paymentDay,
	String startDate,
	String endDate
) {
}
