package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
	public ForbiddenException(String message) {
		super(message);
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.FORBIDDEN.value();
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.FORBIDDEN;
	}

	@Override
	public String getCustomMessage() {
		return "Forbidden.\n" + super.getMessage();
	}
}
