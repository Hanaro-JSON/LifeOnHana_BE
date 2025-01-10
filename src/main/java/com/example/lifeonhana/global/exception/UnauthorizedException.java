package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class UnauthorizedException extends BaseException {
	public UnauthorizedException(String message) {
		super(message);
	}

	@Override
	public HttpStatusCode getStatusCode() {
		return HttpStatus.UNAUTHORIZED;
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
