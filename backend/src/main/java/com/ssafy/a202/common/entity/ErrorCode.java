package com.ssafy.a202.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러 (COMMON)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾지 못하였습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    ORGANIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "기관을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}