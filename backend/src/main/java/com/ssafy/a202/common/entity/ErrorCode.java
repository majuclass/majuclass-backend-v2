package com.ssafy.a202.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러 (COMMON)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다");

    private final HttpStatus status;
    private final String message;
}