package com.example.lifeonhana;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {
	private int status;
	private String code;
	private String message;
	private T data;

	public static <T> ApiResult<T> success(T data) {
		return ApiResult.<T>builder()
			.status(HttpStatus.OK.value())
			.code("SUCCESS")
			.message("요청 처리 성공")
			.data(data)
			.build();
	}

	public static ApiResult<?> error(ErrorCode errorCode) {
		return ApiResult.builder()
			.status(errorCode.getHttpStatus().value())
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.build();
	}
}
