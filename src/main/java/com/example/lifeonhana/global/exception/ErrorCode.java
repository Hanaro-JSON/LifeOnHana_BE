package com.example.lifeonhana.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 성공
    SUCCESS(HttpStatus.OK, "S200", "요청이 성공적으로 처리되었습니다"),
    LOGOUT_SUCCESS(HttpStatus.OK, "A200", "로그아웃 성공"),
    CREATED(HttpStatus.CREATED, "S201", "리소스가 성공적으로 생성됨"),

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다"),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "A003", "잘못된 토큰 형식"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "만료된 토큰"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "사용자를 찾을 수 없습니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A002", "잘못된 비밀번호입니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 리프레시 토큰입니다"),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A004", "토큰이 일치하지 않습니다"),
    TOKEN_VALIDATION_FAILED(HttpStatus.UNAUTHORIZED, "A005", "토큰 검증 실패"),
    LOGOUT_ERROR(HttpStatus.BAD_REQUEST, "A006", "로그아웃 처리 중 오류 발생"),

    // 계좌
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC001", "계좌를 찾을 수 없습니다"),
    MAIN_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC008", "메인 계좌를 찾을 수 없습니다"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "AC002", "잔액이 부족합니다"),
    SALARY_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "AC003", "하나 월급 계좌를 찾을 수 없습니다"),
    NEGATIVE_TRANSFER_AMOUNT(HttpStatus.BAD_REQUEST, "AC004", "이체 금액은 0보다 커야 합니다"),
    TRANSFER_SAME_ACCOUNT(HttpStatus.BAD_REQUEST, "AC005", "동일 계좌로의 이체는 불가능합니다"),
    INTEREST_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AC006", "이자 계산 중 오류가 발생했습니다"),

    // 기사
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "AR001", "게시글을 찾을 수 없습니다"),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "AR002", "잘못된 카테고리입니다"),
    ARTICLE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "AR004", "게시글 수정 권한이 없습니다"),
    ARTICLE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "AR005", "게시글 삭제 권한이 없습니다"),
    INVALID_ARTICLE_CONTENT(HttpStatus.BAD_REQUEST, "AR006", "게시글 내용이 유효하지 않습니다"),

    // 검색 관련
    SEARCH_TYPE_INVALID(HttpStatus.BAD_REQUEST, "AR007", "잘못된 검색 타입입니다"),

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청 형식입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "C002", "요청 검증 실패"),

    // Redis
    REDIS_DATA_INVALID(HttpStatus.BAD_REQUEST, "R001", "잘못된 Redis 데이터 형식"),

    // 서버
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S500", "서버 내부 오류"),

    // 파일 업로드 관련
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드 실패"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F002", "허용되지 않은 파일 형식입니다"),

    // 기사 관련
    ARTICLE_NOT_FOUND_ID(HttpStatus.NOT_FOUND, "A001", "존재하지 않는 ID입니다"),

    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "H001", "로그인이 필요한 서비스입니다"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "H002", "올바른 년월 형식이 아닙니다 (YYYY-MM)"),

    // 대출 관련
    LOAN_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "L001", "유효하지 않은 대출 금액입니다"),
    LOAN_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "L002", "대출 사유는 필수 항목입니다"),

    // 마이데이터 관련
    MYDATA_NOT_FOUND(HttpStatus.BAD_REQUEST, "M001", "마이데이터 정보가 존재하지 않습니다"),

    // 대출 상품 관련
    LOAN_PRODUCTS_NOT_FOUND(HttpStatus.NOT_FOUND, "L003", "대출 상품이 존재하지 않습니다"),
    NO_RECOMMENDED_PRODUCTS(HttpStatus.NOT_FOUND, "L004", "추천된 대출 상품이 없습니다"),

    // Flask API 관련
    FLASK_RESPONSE_EMPTY(HttpStatus.BAD_REQUEST, "F001", "Flask API 응답 데이터 없음"),
    FLASK_RESPONSE_INVALID(HttpStatus.BAD_REQUEST, "F002", "Flask API 응답 형식 오류"),
    FLASK_API_ERROR(HttpStatus.BAD_GATEWAY, "F003", "외부 API 호출 실패"),

    // 유효성 검사 오류
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "V400", "입력값이 유효하지 않습니다"),

    // 동기화 관련
    SYNC_ARTICLE_LIKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S500", "게시글 좋아요 동기화 실패"),
    SYNC_PRODUCT_LIKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S501", "상품 좋아요 동기화 실패"),

    // 목돈 관련
    LUMP_SUM_CREATED(HttpStatus.CREATED, "L201", "목돈 인출 신청이 완료되었습니다"),

    // 지갑 관련
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "W404", "하나지갑이 존재하지 않습니다"),

    // 상품 목록
    PRODUCT_LIST_SUCCESS(HttpStatus.OK, "P200", "상품 목록 조회 성공"),

    // 상품 상세
    SAVING_PRODUCT_DETAIL_SUCCESS(HttpStatus.OK, "P201", "예적금 상품 상세 조회 성공"),
    LOAN_PRODUCT_DETAIL_SUCCESS(HttpStatus.OK, "P202", "대출 상품 상세 조회 성공"),
    LIFE_PRODUCT_DETAIL_SUCCESS(HttpStatus.OK, "P203", "라이프 상품 상세 조회 성공"),

    // 공통
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P404", "상품을 찾을 수 없습니다"),

    // 분석 관련
    INSIGHT_ANALYSIS_SUCCESS(HttpStatus.OK, "I200", "상품 분석이 완료되었습니다"),
    ANALYSIS_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "I503", "분석 서비스가 일시적으로 중단되었습니다");

    

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
} 