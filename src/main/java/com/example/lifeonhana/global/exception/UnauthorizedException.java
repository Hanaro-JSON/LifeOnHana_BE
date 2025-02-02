package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
	public UnauthorizedException(ErrorCode errorCode) {
		super(errorCode);
	}

	public UnauthorizedException(ErrorCode errorCode, Object data) {
		super(errorCode, data);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.UNAUTHORIZED.value();
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.UNAUTHORIZED;
	}

	@Override
	public String getCustomMessage() {
		return "Unauthorized.\n" + super.getMessage();
	}
}
