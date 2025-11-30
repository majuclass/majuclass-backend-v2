package com.ssafy.a202.domain.student.dto.response;

import com.ssafy.a202.domain.student.entity.Student;

import java.time.LocalDateTime;

public record StudentPreviewResponse(
        Long studentId,
        String fullName,
        String organizationName,
        String teacherFullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StudentPreviewResponse of(Student student) {
        return new StudentPreviewResponse(
                student.getId(),
                student.getFullName(),
                student.getOrganization().getOrgName(),
                student.getUser().getFullName(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}