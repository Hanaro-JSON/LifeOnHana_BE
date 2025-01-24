package com.example.lifeonhana.dto.response;

import com.example.lifeonhana.global.exception.ErrorCode;

public record ErrorResponse(
    String code,
    String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
} 