package com.example.lifeonhana.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class WalletDTO {
	/*
	* walletId 급여정보id
	* walletAmount 급여금액
	* paymentDay 급여일
	* startDate 시작일 (yyyy-mm)
	* endDate 종료일 (yyyy-mm)
	* */
	long walletId;
	long walletAmount;
	String paymentDay;
	String startDate;
	String endDate;
}
