package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
	private final Object data;

	protected BaseException(String message) {
		super(message, null);
		this.data = null;
	}

	protected BaseException(String message, Object data) {
		super(message);
		this.data = data;
	}

	public abstract int getStatusCode();
	public abstract HttpStatus getHttpStatus();
	public String getCustomMessage() {
		return getMessage();
	}
}
