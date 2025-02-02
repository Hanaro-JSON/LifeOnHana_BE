package com.example.lifeonhana;

import org.springframework.http.HttpStatus;

import com.example.lifeonhana.global.exception.ErrorCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResult<T> {
	private String code;
	private HttpStatus status;
	private String message;
	private T data;
	private String customMessage;

	public static <T> ApiResultBuilder<T> builder() {
		return new ApiResultBuilder<>();
	}

	public static class ApiResultBuilder<T> {
		private String code;
		private HttpStatus status;
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
			return new ApiResult<>(code, status, message, data, customMessage);
		}

		public <U> ApiResultBuilder<U> data(U data) {
			this.data = (T) data;
			return (ApiResultBuilder<U>) this;
		}
	}

	public static <T> ApiResult<T> success() {
		return success(ErrorCode.SUCCESS, null);
	}

	public static <T> ApiResult<T> success(T data) {
		return success(ErrorCode.SUCCESS, data);
	}

	public static <T> ApiResult<T> success(ErrorCode successCode, T data) {
		return ApiResult.<T>builder()
			.code(successCode.getCode())
			.status(successCode.getHttpStatus())
			.message(successCode.getMessage())
			.data(data)
			.build();
	}

	public static ApiResult<?> error(ErrorCode errorCode) {
		return error(errorCode, null);
	}

	public static ApiResult<?> error(ErrorCode errorCode, Object errorData) {
		return ApiResult.builder()
			.code(errorCode.getCode())
			.status(errorCode.getHttpStatus())
			.message(errorCode.getMessage())
			.data(errorData)
			.build();
	}

	public String getCustomMessage() {
		return customMessage != null ? customMessage : this.message;
	}
}
