package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
	protected BaseException(String message) {
		super(message);
	}
	public abstract int getStatusCode();
	public abstract HttpStatus getHttpStatus();
	public String getCustomMessage() {
		return getMessage();
	}
}
