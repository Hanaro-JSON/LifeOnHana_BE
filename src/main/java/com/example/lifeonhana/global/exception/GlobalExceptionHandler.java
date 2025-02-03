package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingRequestHeaderException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// 1. 모든 커스텀 예외 처리 (핵심)
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResult<?>> handleBaseException(BaseException e) {
		log.error("Business Exception: {}", e.getErrorCode().name(), e);
		return buildResponse(e.getErrorCode(), e.getDetails());
	}

	// 2. 스프링 프레임워크 예외 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResult<?>> handleValidation(MethodArgumentNotValidException e) {
		Map<String, String> errors = e.getFieldErrors().stream()
			.collect(Collectors.toMap(
				FieldError::getField,
				fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse("")
			));
		return buildResponse(ErrorCode.INVALID_INPUT, errors);
	}

	// 3. 처리되지 않은 모든 예외
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResult<?>> handleAll(Exception e) {
		log.error("Unhandled Exception: ", e);
		return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ApiResult<?>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
		return buildResponse(ErrorCode.UNAUTHORIZED, "인증 헤더가 존재하지 않습니다");
	}

	// 공통 응답 생성 메서드
	private ResponseEntity<ApiResult<?>> buildResponse(ErrorCode errorCode, Object data) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResult.error(errorCode, data));
	}

	private ResponseEntity<ApiResult<?>> buildResponse(ErrorCode errorCode, String message) {
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(ApiResult.error(errorCode, message));
	}
}
