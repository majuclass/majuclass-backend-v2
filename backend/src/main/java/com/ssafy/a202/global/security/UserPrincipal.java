package com.ssafy.a202.global.security;

import com.ssafy.a202.global.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 인증된 사용자 정보를 담는 Principal 객체
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {

    private final Long userId;
    private final String username;
    private final Role role;
}