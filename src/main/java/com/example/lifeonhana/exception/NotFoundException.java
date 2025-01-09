package com.example.lifeonhana.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class NotFoundException extends BaseException{
	public NotFoundException(String message) {
		super(message);
	}

	@Override
	public HttpStatusCode getStatusCode() {
		return HttpStatus.NOT_FOUND;
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
