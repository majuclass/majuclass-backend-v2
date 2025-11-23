package com.ssafy.a202.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 공통
    SUCCESS_DEFAULT("요청이 성공적으로 처리되었습니다");

    private final String message;
}
