package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
	public ForbiddenException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ForbiddenException(ErrorCode errorCode, Object data) {
		super(errorCode, data);
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
