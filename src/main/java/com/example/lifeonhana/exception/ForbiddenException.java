package com.example.lifeonhana.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ForbiddenException extends BaseException {
	public ForbiddenException(String message) {
		super(message);
	}

	@Override
	public HttpStatusCode getStatusCode() {
		return HttpStatus.FORBIDDEN;
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
