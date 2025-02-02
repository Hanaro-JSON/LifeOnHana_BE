package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.lifeonhana.ApiResult;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResult<?>> handleBaseException(BaseException e) {
		return ResponseEntity.status(e.getHttpStatus())
			.body(ApiResult.error(e.getErrorCode()));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiResult<?>> handleUnauthorizedException(UnauthorizedException exception) {
		ApiResult<?> response = ApiResult.builder()
			.status(HttpStatus.UNAUTHORIZED)
			.code(exception.getErrorCode().getCode())
			.message(exception.getCustomMessage())
			.data(exception.getData())
			.build();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResult<?>> handleAll(Exception e) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResult.error(errorCode));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResult<?>> handleIllegalArgument(IllegalArgumentException e) {
		return createErrorResponse(ErrorCode.INVALID_REQUEST, e.getMessage());
	}

	private ResponseEntity<ApiResult<?>> createErrorResponse(ErrorCode errorCode, String message) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResult.builder()
				.status(errorCode.getHttpStatus())
				.code(errorCode.getCode())
				.message(message)
				.build());
	}
}
