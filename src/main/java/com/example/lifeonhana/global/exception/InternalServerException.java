package com.example.lifeonhana.global.exception;

import com.example.lifeonhana.global.exception.BaseException;

public class InternalServerException extends BaseException {
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InternalServerException(ErrorCode errorCode, Object data) {
        super(errorCode, data);
    }
} 