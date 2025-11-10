package com.ssafy.a202.domain.user.service;

import com.ssafy.a202.domain.user.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.user.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.user.dto.response.StudentResponse;
import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.repository.StudentRepository;
import com.ssafy.a202.domain.user.repository.UserRepository;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.constants.Role;
import com.ssafy.a202.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 학생 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    /**
     * 담당 학생 목록 조회
     * - ADMIN: 자신의 학교의 모든 학생 조회
     * - USER: 자신이 담당하는 학생만 조회
     */
    @Override
    public List<StudentResponse> getStudents(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 목록 조회 (권한에 따라 다르게)
        List<Student> students;
        if (isAdmin(user)) {
            // 관리자: 학교의 모든 학생 조회
            students = studentRepository.findBySchoolAndIsDeletedFalse(user.getSchool());
            log.debug("Student list retrieved by ADMIN: userId={}, schoolId={}, count={}",
                    userId, user.getSchool().getId(), students.size());
        } else {
            // 일반 선생님: 담당 학생만 조회
            students = studentRepository.findByUserAndIsDeletedFalse(user);
            log.debug("Student list retrieved by USER: userId={}, count={}", userId, students.size());
        }

        // 3. 응답 생성
        return students.stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 학생 상세 조회
     * - ADMIN: 자신의 학교 학생이면 조회 가능
     * - USER: 자신이 담당하는 학생만 조회 가능
     */
    @Override
    public StudentResponse getStudent(Long userId, Long studentId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        log.debug("Student detail retrieved: userId={}, studentId={}",
                userId, studentId);

        // 4. 응답 생성
        return StudentResponse.from(student);
    }

    /**
     * 학생 추가
     */
    @Override
    @Transactional
    public StudentResponse createStudent(Long userId, StudentCreateRequest request) {
        // 1. 선생님 조회
        User teacher = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 생성 (학교는 선생님의 학교로 자동 설정)
        Student student = Student.builder()
                .name(request.getName())
                .school(teacher.getSchool())
                .user(teacher)
                .build();

        // 3. 저장
        Student savedStudent = studentRepository.save(student);

        log.info("Student created: userId={}, studentId={}, studentName={}",
                userId, savedStudent.getId(), savedStudent.getName());

        // 4. 응답 생성
        return StudentResponse.from(savedStudent);
    }

    /**
     * 학생 정보 수정
     * - ADMIN: 자신의 학교 학생이면 수정 가능
     * - USER: 자신이 담당하는 학생만 수정 가능
     */
    @Override
    @Transactional
    public StudentResponse updateStudent(Long userId, Long studentId, StudentUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 이름 수정
        if (request.getName() != null && !request.getName().isBlank()) {
            student.updateName(request.getName());
        }

        // 5. 담당 선생님 변경
        if (request.getUserId() != null) {
            User newTeacher = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 새 선생님이 같은 학교인지 확인
            if (!student.getSchool().getId().equals(newTeacher.getSchool().getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            student.changeTeacher(newTeacher);
        }

        log.info("Student updated: userId={}, studentId={}",
                userId, studentId);

        // 6. 응답 생성
        return StudentResponse.from(student);
    }

    /**
     * 학생 삭제
     * - ADMIN: 자신의 학교 학생이면 삭제 가능
     * - USER: 자신이 담당하는 학생만 삭제 가능
     */
    @Override
    @Transactional
    public void deleteStudent(Long userId, Long studentId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 삭제 처리 (soft delete)
        student.delete();

        log.info("Student deleted: userId={}, studentId={}",
                userId, studentId);
    }

    // ================================
    // Private Helper Methods
    // ================================

    /**
     * 관리자 권한 확인
     */
    private boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    /**
     * 학생 접근 권한 검증
     * - ADMIN: 같은 학교 학생이면 접근 가능
     * - USER: 담당 학생만 접근 가능
     */
    private void validateStudentAccess(User user, Student student) {
        if (isAdmin(user)) {
            // 관리자: 같은 학교 학생인지 확인
            if (!student.getSchool().getId().equals(user.getSchool().getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            // 일반 선생님: 담당 학생인지 확인
            if (!student.getUser().getId().equals(user.getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }
    }
}