package com.ssafy.a202.domain.student.dto.response;

import com.ssafy.a202.domain.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학생 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 응답")
public class StudentResponse {

    @Schema(description = "학생 ID", example = "1")
    private Long studentId;

    @Schema(description = "학생 이름", example = "김학생")
    private String name;

    @Schema(description = "담당 선생님 ID", example = "1")
    private Long userId;

    @Schema(description = "담당 선생님 이름", example = "홍길동")
    private String userName;

    @Schema(description = "학교 이름", example = "서울초등학교")
    private String schoolName;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    public static StudentResponse from(Student student) {
        return StudentResponse.builder()
                .studentId(student.getId())
                .name(student.getName())
                .userId(student.getUser().getId())
                .userName(student.getUser().getName())
                .schoolName(student.getSchool().getSchoolName())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}