package com.ssafy.a202.domain.auth.dto.response;

import com.ssafy.a202.domain.user.entity.User;

public record LoginResponse(
        Long userId,
        String username,
        String fullName,
        String accessToken,
        String refreshToken
) {
    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                accessToken,
                refreshToken
        );
    }
}
