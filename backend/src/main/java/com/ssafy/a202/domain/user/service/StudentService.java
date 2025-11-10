package com.ssafy.a202.domain.user.service;

import com.ssafy.a202.domain.user.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.user.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.user.dto.response.StudentResponse;

import java.util.List;

/**
 * 학생 서비스 인터페이스
 */
public interface StudentService {

    /**
     * 담당 학생 목록 조회
     * @param userId 선생님 ID
     * @return 담당 학생 목록
     */
    List<StudentResponse> getStudents(Long userId);

    /**
     * 학생 상세 조회
     * @param userId 선생님 ID
     * @param studentId 학생 ID
     * @return 학생 상세 정보
     */
    StudentResponse getStudent(Long userId, Long studentId);

    /**
     * 학생 추가
     * @param userId 선생님 ID
     * @param request 학생 추가 정보
     * @return 추가된 학생 정보
     */
    StudentResponse createStudent(Long userId, StudentCreateRequest request);

    /**
     * 학생 정보 수정
     * @param userId 선생님 ID (요청한 선생님)
     * @param studentId 학생 ID
     * @param request 수정할 정보
     * @return 수정된 학생 정보
     */
    StudentResponse updateStudent(Long userId, Long studentId, StudentUpdateRequest request);

    /**
     * 학생 삭제
     * @param userId 선생님 ID
     * @param studentId 학생 ID
     */
    void deleteStudent(Long userId, Long studentId);
}