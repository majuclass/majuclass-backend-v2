package com.ssafy.a202.domain.user.dto.response;

import com.ssafy.a202.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 정보 수정 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 수정 응답")
public class UserUpdateResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자명", example = "teacher01")
    private String username;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "teacher@school.com")
    private String email;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}