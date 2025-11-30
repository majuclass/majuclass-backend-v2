package com.ssafy.a202.domain.student.service;

import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.roleAop.PermissionAction;
import com.ssafy.a202.common.roleAop.studentPermission.CheckStudentPermission;
import com.ssafy.a202.domain.student.dto.request.StudentRequest;
import com.ssafy.a202.domain.student.dto.response.StudentCreateResponse;
import com.ssafy.a202.domain.student.dto.response.StudentPreviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface StudentService {
    @Transactional
    StudentCreateResponse create(Long userId, StudentRequest request);

    PageResponse<StudentPreviewResponse> getStudents(Long userId, Pageable pageable);

    @CheckStudentPermission(PermissionAction.UPDATE)
    @Transactional
    void update(Long userId, Long studentId, StudentRequest request);

    @CheckStudentPermission(PermissionAction.DELETE)
    @Transactional
    void delete(Long userId, Long studentId);
}
