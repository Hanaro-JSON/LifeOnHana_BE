package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	private final ErrorCode errorCode;
	private final transient Object details;

	public BaseException(ErrorCode errorCode) {
		this(errorCode, null, null);
	}

	public BaseException(ErrorCode errorCode, Object details) {
		this(errorCode, details, null);
	}

	public BaseException(ErrorCode errorCode, Throwable cause) {
		this(errorCode, null, cause);
	}

	public BaseException(ErrorCode errorCode, Object details, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
		this.details = details;
	}

	public int getStatusCode() {
		return errorCode.getHttpStatus().value();
	}

	public HttpStatus getHttpStatus() {
		return errorCode.getHttpStatus();
	}

	public String getCustomMessage() {
		return errorCode.getMessage();
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}
}
