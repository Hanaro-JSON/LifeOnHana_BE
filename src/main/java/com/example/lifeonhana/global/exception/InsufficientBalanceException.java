package com.example.lifeonhana.global.exception;

public class InsufficientBalanceException extends BaseException {
	public InsufficientBalanceException(ErrorCode errorCode) {
		super(errorCode);
	}
	
	public InsufficientBalanceException() {
		super(ErrorCode.INSUFFICIENT_BALANCE);
	}
	
	public InsufficientBalanceException(Object data) {
		super(ErrorCode.INSUFFICIENT_BALANCE, data);
	}
}
