package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
	private final ErrorCode errorCode;
	private final Object data;

	public BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.data = null;
	}

	public BaseException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
		this.data = null;
	}

	public BaseException(ErrorCode errorCode, Object data) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.data = data;
	}

	public BaseException(ErrorCode errorCode, Object data, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
		this.data = data;
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
