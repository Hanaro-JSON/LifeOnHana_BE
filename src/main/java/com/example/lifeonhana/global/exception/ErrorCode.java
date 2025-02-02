package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 인증 관련 에러
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 접근입니다."),
    
    // 계좌 관련 에러
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC001", "계좌를 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "AC002", "잔액이 부족합니다."),
    SAME_ACCOUNT_TRANSFER(HttpStatus.BAD_REQUEST, "AC003", "출금 계좌와 입금 계좌가 동일할 수 없습니다."),
    NEGATIVE_TRANSFER_AMOUNT(HttpStatus.BAD_REQUEST, "AC004", "이체 금액은 0보다 커야 합니다."),
    ACCOUNT_LIST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AC005", "계좌 목록 조회 실패"),
    SALARY_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC006", "급여 계좌를 찾을 수 없습니다"),
    TRANSFER_FAILED(HttpStatus.BAD_REQUEST, "AC007", "계좌 이체 처리 실패"),
    MAIN_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC008", "메인 계좌를 찾을 수 없습니다"),
    TRANSFER_SAME_ACCOUNT(HttpStatus.BAD_REQUEST, "AC009", "출금/입금 계좌가 동일합니다"),
    INTEREST_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AC010", "이자 계산 실패"),
    
    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    // 공통 검증 오류
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청 형식입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "C002", "요청 검증 실패"),

    // 인증/인가
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다"),

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류"),

    // 권한 관련 오류
    FORBIDDEN(HttpStatus.FORBIDDEN, "A001", "접근 권한이 없습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A002", "인증이 필요합니다"),
    
    // 대출 관련
    LOAN_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "L001", "대출 사유는 필수 항목입니다"),
    INVALID_LOAN_AMOUNT(HttpStatus.BAD_REQUEST, "L002", "대출 금액은 0보다 커야 합니다"),

    // 기사 관련
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "AR001", "게시글을 찾을 수 없습니다"),

    // 상품 관련
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다"),

    // 리소스 관련 오류
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "요청한 리소스를 찾을 수 없습니다"),

    // 인증 관련
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "A003", "토큰 형식이 올바르지 않습니다"),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 액세스 토큰"),

    // Redis 관련
    REDIS_DATA_INVALID(HttpStatus.BAD_REQUEST, "R001", "잘못된 Redis 데이터 형식"),
    SYNC_ARTICLE_LIKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R002", "좋아요 동기화 실패"),
    SYNC_PRODUCT_LIKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R003", "상품 좋아요 동기화 실패");
        

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
} 