package com.ssafy.a202.domain.auth.dto.response;

import com.ssafy.a202.domain.user.entity.User;

public record SignupResponse(
        Long userId,
        String username,
        String fullName
) {
    public static SignupResponse of(User savedUser) {
        return new SignupResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName()
        );
    }
}
