package com.ssafy.a202.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러 (COMMON)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력 값 검증에 실패했습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾지 못하였습니다."),

    // 회원가입 에러
    USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // 기관 에러
    ORGANIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "기관을 찾을 수 없습니다."),

    // 인증 및 검증
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

    // S3 에러
    S3_PRESIGNED_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 Presigned URL 생성에 실패했습니다."),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다."),
    S3_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "S3에 파일이 존재하지 않습니다. 파일을 먼저 업로드해주세요."),

    // 시나리오
    SCENARIO_NOT_FOUND(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."),
    SCENARIO_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "시나리오를 수정할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}