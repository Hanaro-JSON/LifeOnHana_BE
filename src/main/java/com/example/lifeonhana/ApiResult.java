package com.example.lifeonhana;

import org.springframework.http.HttpStatus;

import com.example.lifeonhana.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class ApiResult<T> {
	private HttpStatus status;
	private String code;
	private String message;
	private T data;
	private String customMessage;

	public static <T> ApiResultBuilder<T> builder() {
		return new ApiResultBuilder<>();
	}

	public static class ApiResultBuilder<T> {
		private HttpStatus status;
		private String code;
		private String message;
		private T data;
		private String customMessage;

		public ApiResultBuilder<T> code(String code) {
			this.code = code;
			return this;
		}

		public ApiResultBuilder<T> errorCode(ErrorCode errorCode) {
			this.status = errorCode.getHttpStatus();
			this.code = errorCode.getCode();
			this.message = errorCode.getMessage();
			return this;
		}

		public ApiResultBuilder<T> message(String message) {
			this.customMessage = message;
			return this;
		}

		public ApiResult<T> build() {
			return new ApiResult<>(status, code, message, data, customMessage);
		}
	}

	public static <T> ApiResult<T> success(T data) {
		return ApiResult.<T>builder()
			.status(HttpStatus.OK)
			.code("SUCCESS")
			.message("요청 처리 성공")
			.data(data)
			.build();
	}

	public static ApiResult<?> error(ErrorCode errorCode) {
		return ApiResult.builder()
			.status(errorCode.getHttpStatus())
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.build();
	}

	public String getCustomMessage() {
		return customMessage != null ? customMessage : this.message;
	}
}
