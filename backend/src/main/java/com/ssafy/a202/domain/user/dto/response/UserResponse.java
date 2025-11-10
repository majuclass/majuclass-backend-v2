package com.ssafy.a202.domain.user.dto.response;

import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.global.constants.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 응답")
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자명", example = "teacher01")
    private String username;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "teacher@school.com")
    private String email;

    @Schema(description = "역할", example = "USER")
    private Role role;

    @Schema(description = "학교 ID", example = "1")
    private Long schoolId;

    @Schema(description = "학교 이름", example = "서울초등학교")
    private String schoolName;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .schoolId(user.getSchool().getId())
                .schoolName(user.getSchool().getSchoolName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}