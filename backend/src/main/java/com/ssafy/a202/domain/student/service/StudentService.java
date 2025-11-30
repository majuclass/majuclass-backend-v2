package com.ssafy.a202.domain.student.service;

import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.common.roleAop.PermissionAction;
import com.ssafy.a202.common.roleAop.studentPermission.CheckStudentPermission;
import com.ssafy.a202.domain.student.dto.request.StudentRequest;
import com.ssafy.a202.domain.student.dto.response.StudentCreateResponse;
import com.ssafy.a202.domain.student.dto.response.StudentPreviewResponse;
import com.ssafy.a202.domain.student.entity.Student;
import com.ssafy.a202.domain.student.repository.StudentRepository;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.entity.UserRole;
import com.ssafy.a202.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public StudentCreateResponse create(Long userId, StudentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User homeroomTeacher;

        // 역할에 따른 담임 교사 설정
        if (user.getRole() == UserRole.ORG_ADMIN || user.getRole() == UserRole.ADMIN) {
            // ORG_ADMIN: request의 userId 사용 (자유롭게 지정 가능)
            if (request.userId() == null) {
                throw new CustomException(ErrorCode.TEACHER_ID_REQUIRED);
            }
            homeroomTeacher = userRepository.findById(request.userId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 같은 기관인지 검증
            if (!homeroomTeacher.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new CustomException(ErrorCode.PERMISSION_DENIED);
            }
        } else if (user.getRole() == UserRole.TEACHER) {
            // TEACHER: 자기 자신으로만 설정 (request 무시)
            homeroomTeacher = user;
        } else {
            throw new CustomException(ErrorCode.PERMISSION_DENIED);
        }

        Student student = Student.of(homeroomTeacher, request);
        studentRepository.save(student);

        return StudentCreateResponse.of(student);
    }

    public PageResponse<StudentPreviewResponse> getStudents(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<Student> studentPage = switch (user.getRole()) {
            case ADMIN -> studentRepository.findByDeletedAtIsNull(pageable);
            case ORG_ADMIN -> studentRepository.findByOrganizationIdAndDeletedAtIsNull(user.getOrganization().getId(), pageable);
            case TEACHER -> studentRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        };

        List<StudentPreviewResponse> responseList = new ArrayList<>();
        for (Student student : studentPage.getContent()) {
            StudentPreviewResponse response = StudentPreviewResponse.of(student);
            responseList.add(response);
        }

        return PageResponse.of(studentPage, responseList);
    }

    @CheckStudentPermission(PermissionAction.UPDATE)
    @Transactional
    public void update(Long userId, String studentId, StudentRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Student student = studentRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        User homeroomTeacher = null;

        // 담임 교사 변경 로직
        if (request.userId() != null) {
            // ORG_ADMIN: request의 userId 사용 가능
            if (user.getRole() == UserRole.ORG_ADMIN) {
                homeroomTeacher = userRepository.findById(request.userId())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                // 같은 기관인지 검증
                if (!homeroomTeacher.getOrganization().getId().equals(user.getOrganization().getId())) {
                    throw new CustomException(ErrorCode.PERMISSION_DENIED);
                }

            } else if (user.getRole() == UserRole.ADMIN) {
                // ADMIN: 모든 교사로 변경 가능
                homeroomTeacher = userRepository.findById(request.userId())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            }

            // TEACHER는 담임 교사 변경 불가 (request.userId 무시)
        }

        student.update(homeroomTeacher, request);
    }

    @CheckStudentPermission(PermissionAction.DELETE)
    @Transactional
    public void delete(Long userId, String studentId) {
        Student student = studentRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        student.delete();
    }
}
