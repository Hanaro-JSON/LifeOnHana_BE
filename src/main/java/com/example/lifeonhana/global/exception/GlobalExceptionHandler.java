package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.lifeonhana.ApiResult;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<?> handleApiException(BaseException exception) {
		ApiResult response = ApiResult.builder()
			.code(exception.getStatusCode())
			.status(exception.getHttpStatus())
			.message(exception.getCustomMessage())
			.data(exception.getData())
			.build();
		return ResponseEntity.status(exception.getHttpStatus()).body(response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException exception) {
		ApiResult response = ApiResult.builder()
			.code(HttpStatus.BAD_REQUEST.value())
			.status(HttpStatus.BAD_REQUEST)
			.message(exception.getMessage())
			.data(null)
			.build();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException exception) {
		ApiResult response = ApiResult.builder()
			.code(HttpStatus.UNAUTHORIZED.value())
			.status(HttpStatus.UNAUTHORIZED)
			.message(exception.getMessage())
			.data(null)
			.build();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception exception) {
		ApiResult response = ApiResult.builder()
			.code(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.message("서버 내부 오류가 발생했습니다." + exception.getMessage())
			.data(null)
			.build();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
