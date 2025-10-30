package com.ssafy.a202.global.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드와 HTTP 상태, 메시지를 정의하는 열거형
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러 (COMMON)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "요청 시간이 초과되었습니다"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출에 실패했습니다"),

    // 인증/인가 관련 (AUTH)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 로그인 정보입니다"),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "인증 토큰이 누락되었습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "비활성화된 계정입니다"),
    USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 사용자 ID입니다"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    SCHOOL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 학교입니다"),

    // 시나리오 관련 (SCENARIO)
    SCENARIO_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 시나리오를 찾을 수 없습니다"),
    SEQUENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 시퀀스를 찾을 수 없습니다"),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 옵션을 찾을 수 없습니다"),
    INVALID_OPTION_SELECTED(HttpStatus.BAD_REQUEST, "유효하지 않은 옵션이 선택되었습니다"),
    CORRECT_OPTION_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "시퀀스의 정답을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String message;
}