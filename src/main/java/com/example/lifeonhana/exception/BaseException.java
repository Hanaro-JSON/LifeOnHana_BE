package com.example.lifeonhana.exception;

import org.aspectj.bridge.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
	protected BaseException(String message) {
		super(message);
	}
	public abstract HttpStatusCode getStatusCode();
	public abstract HttpStatus getHttpStatus();
	public String getCustomMessage() {
		return getMessage();
	};
}
