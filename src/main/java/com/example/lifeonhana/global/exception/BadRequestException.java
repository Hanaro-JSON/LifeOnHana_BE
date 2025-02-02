package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

	public BadRequestException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BadRequestException(ErrorCode errorCode, Object data) {
		super(errorCode, data);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.BAD_REQUEST.value();
	}

	@Override
	public String getCustomMessage() {
		return "Bad Request.\n" + super.getMessage();
	}
}
