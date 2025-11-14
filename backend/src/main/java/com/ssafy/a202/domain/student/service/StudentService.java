package com.ssafy.a202.domain.student.service;

import com.ssafy.a202.domain.student.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.student.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.student.dto.response.CalendarMonthlyResponse;
import com.ssafy.a202.domain.student.dto.response.DailySessionListResponse;
import com.ssafy.a202.domain.student.dto.response.SessionSequenceStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentDashboardStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentResponse;
import com.ssafy.a202.domain.student.dto.response.StudentSessionsResponse;
import com.ssafy.a202.global.constants.SessionStatus;

import java.time.LocalDate;
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

    /**
     * 학생 대시보드 통계 조회 (카테고리별 비율)
     * @param userId 선생님 ID
     * @param studentId 학생 ID
     * @param year 년도
     * @param month 월 (1-12)
     * @return 카테고리별 통계 데이터
     */
    StudentDashboardStatsResponse getStudentDashboardStats(Long userId, Long studentId, int year, int month);

    /**
     * 학생 세션 목록 조회
     * @param userId 선생님 ID
     * @param studentId 학생 ID
     * @param year 년도
     * @param month 월 (1-12)
     * @param categoryId 카테고리 필터 (optional)
     * @param status 세션 상태 필터 (optional)
     * @return 세션 목록 데이터
     */
    StudentSessionsResponse getStudentSessions(Long userId, Long studentId, int year, int month, Long categoryId, SessionStatus status);

    /**
     * 세션의 시퀀스별 통계 조회
     * @param userId 선생님 ID
     * @param sessionId 세션 ID
     * @return 시퀀스별 통계 데이터
     */
    SessionSequenceStatsResponse getSessionSequenceStats(Long userId, Long sessionId);

    /**
     * 월별 달력 데이터 조회 (캐시 적용)
     * 담당 학생들의 일별 세션 수 통계
     * @param userId 선생님 ID
     * @param year 년도
     * @param month 월 (1-12)
     * @return 월별 달력 데이터
     */
    CalendarMonthlyResponse getMonthlyCalendar(Long userId, int year, int month);

    /**
     * 특정 날짜의 특정 학생 세션 목록 조회
     * @param userId 선생님 ID
     * @param studentId 학생 ID
     * @param date 날짜
     * @return 해당 날짜의 세션 목록
     */
    DailySessionListResponse getDailySessions(Long userId, Long studentId, LocalDate date);

    /**
     * 달력 캐시 무효화
     * 새 세션 생성/삭제 시 호출 (해당 날짜의 캐시만 삭제)
     * @param userId 선생님 ID
     * @param date 날짜
     */
    void invalidateCalendarCache(Long userId, LocalDate date);
}