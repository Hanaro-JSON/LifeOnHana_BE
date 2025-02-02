package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.lifeonhana.ApiResult;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResult> handleBaseException(BaseException exception) {
		return ResponseEntity
			.status(exception.getHttpStatus())
			.body(ApiResult.builder()
				.status(exception.getHttpStatus().value())
				.code(exception.getErrorCode())
				.message(exception.getCustomMessage())
				.data(exception.getData())
				.build());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException exception) {
		ApiResult response = ApiResult.builder()
			.status(HttpStatus.UNAUTHORIZED.value())
			.code(exception.getErrorCode())
			.message(exception.getCustomMessage())
			.data(exception.getData())
			.build();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResult> handleException(Exception exception) {
		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResult.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.code("INTERNAL_ERROR")
				.message("서버 내부 오류가 발생했습니다")
				.build());
	}
}
