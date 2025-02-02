package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
	public NotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	public NotFoundException(ErrorCode errorCode, Object data) {
		super(errorCode, data);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.NOT_FOUND.value();
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.NOT_FOUND;
	}

	@Override
	public String getCustomMessage() {
		return "Not Found.\n" + super.getMessage();
	}
}
