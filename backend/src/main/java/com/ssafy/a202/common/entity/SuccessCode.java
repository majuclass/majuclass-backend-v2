package com.ssafy.a202.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 공통
    SUCCESS_DEFAULT("요청이 성공적으로 처리되었습니다."),
    LOGIN_SUCCESS("로그인이 성공적으로 처리되었습니다."),
    SIGNUP_SUCCESS("회원가입이 성공적으로 완료되었습니다."),

    // 시나리오
    SCENARIO_CREATE_SUCCESS("시나리오가 성공적으로 생성되었습니다.");

    private final String message;
}
