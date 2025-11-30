package com.ssafy.a202.domain.student.dto.response;

import com.ssafy.a202.domain.student.entity.Student;

public record StudentCreateResponse(
        Long studentId
) {

    public static StudentCreateResponse of(Student student) {
        return new StudentCreateResponse(
                student.getId()
        );
    }
}
