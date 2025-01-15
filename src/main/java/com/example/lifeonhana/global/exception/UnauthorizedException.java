package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
	public UnauthorizedException(String message) {
		super(message);
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
