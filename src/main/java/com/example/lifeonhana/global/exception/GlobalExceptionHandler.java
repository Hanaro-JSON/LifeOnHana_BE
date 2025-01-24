package com.example.lifeonhana.global.exception;

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

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception exception) {
		return ResponseEntity.internalServerError().body(exception.getMessage());
	}

}
