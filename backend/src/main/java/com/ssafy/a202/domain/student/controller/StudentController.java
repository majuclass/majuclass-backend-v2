package com.ssafy.a202.domain.student.controller;

import com.ssafy.a202.domain.student.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.student.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.student.dto.response.CalendarMonthlyResponse;
import com.ssafy.a202.domain.student.dto.response.DailySessionListResponse;
import com.ssafy.a202.domain.student.dto.response.SessionSequenceStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentDashboardStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentSessionsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentResponse;
import com.ssafy.a202.domain.student.service.StudentService;
import com.ssafy.a202.global.constants.SessionStatus;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import com.ssafy.a202.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 학생 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Student", description = "학생 관리 API")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "학생 목록 조회", description = "학생 목록을 조회합니다. 관리자는 자신의 학교 전체 학생을, 일반 선생님은 담당 학생만 조회할 수 있습니다.")
    @GetMapping
    public ApiResponse<List<StudentResponse>> getStudents(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<StudentResponse> students = studentService.getStudents(userPrincipal.getUserId());
        return ApiResponse.success(SuccessCode.STUDENT_LIST_SUCCESS, students);
    }

    @Operation(summary = "학생 상세 조회", description = "특정 학생의 상세 정보를 조회합니다. 관리자는 자신의 학교 학생을, 일반 선생님은 담당 학생만 조회할 수 있습니다.")
    @GetMapping("/{studentId}")
    public ApiResponse<StudentResponse> getStudent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "학생 ID", example = "1")
            @PathVariable Long studentId) {
        StudentResponse student = studentService.getStudent(userPrincipal.getUserId(), studentId);
        return ApiResponse.success(SuccessCode.STUDENT_DETAIL_SUCCESS, student);
    }

    @Operation(summary = "학생 추가", description = "새로운 학생을 추가합니다. 학교는 자동으로 본인의 학교로 설정되며, 담당 선생님은 본인으로 설정됩니다.")
    @PostMapping("/create")
    public ApiResponse<StudentResponse> createStudent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid StudentCreateRequest request) {
        StudentResponse student = studentService.createStudent(userPrincipal.getUserId(), request);
        return ApiResponse.success(SuccessCode.STUDENT_CREATE_SUCCESS, student);
    }

    @Operation(summary = "학생 정보 수정", description = "학생의 이름 또는 담당 선생님을 수정합니다. 관리자는 자신의 학교 학생을, 일반 선생님은 담당 학생만 수정할 수 있습니다.")
    @PutMapping("/update/{studentId}")
    public ApiResponse<StudentResponse> updateStudent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "학생 ID", example = "1")
            @PathVariable Long studentId,
            @RequestBody @Valid StudentUpdateRequest request) {
        StudentResponse student = studentService.updateStudent(userPrincipal.getUserId(), studentId, request);
        return ApiResponse.success(SuccessCode.STUDENT_UPDATE_SUCCESS, student);
    }

    @Operation(summary = "학생 삭제", description = "학생을 삭제합니다. (Soft Delete) 관리자는 자신의 학교 학생을, 일반 선생님은 담당 학생만 삭제할 수 있습니다.")
    @DeleteMapping("/delete/{studentId}")
    public ApiResponse<Void> deleteStudent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "학생 ID", example = "1")
            @PathVariable Long studentId) {
        studentService.deleteStudent(userPrincipal.getUserId(), studentId);
        return ApiResponse.success(SuccessCode.STUDENT_DELETE_SUCCESS);
    }

    @Operation(
            summary = "학생 대시보드 통계 조회",
            description = "특정 학생의 월별 카테고리별 세션 통계를 조회합니다. (도넛 그래프용)"
    )
    @GetMapping("/dashboard/category-stats/{studentId}")
    public ApiResponse<StudentDashboardStatsResponse> getStudentDashboardStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "학생 ID", example = "1")
            @PathVariable Long studentId,
            @Parameter(description = "조회 년도", example = "2025")
            @RequestParam int year,
            @Parameter(description = "조회 월 (1-12)", example = "1")
            @RequestParam int month
    ) {
        StudentDashboardStatsResponse stats = studentService.getStudentDashboardStats(
                userPrincipal.getUserId(),
                studentId,
                year,
                month
        );
        return ApiResponse.success(SuccessCode.CATEGORY_STATS_SUCCESS, stats);
    }

    @Operation(
            summary = "학생 세션 목록 조회",
            description = "특정 학생의 월별 세션 목록을 조회합니다. 카테고리 및 세션 상태로 필터링이 가능합니다."
    )
    @GetMapping("/dashboard/monthly-sessions/{studentId}")
    public ApiResponse<StudentSessionsResponse> getStudentSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "학생 ID", example = "1")
            @PathVariable Long studentId,
            @Parameter(description = "조회 년도", example = "2025")
            @RequestParam int year,
            @Parameter(description = "조회 월 (1-12)", example = "1")
            @RequestParam int month,
            @Parameter(description = "카테고리 필터 (선택 사항)", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "세션 상태 필터 (선택 사항, IN_PROGRESS/COMPLETED/ABORTED)", example = "COMPLETED")
            @RequestParam(required = false) SessionStatus status
    ) {
        StudentSessionsResponse sessions = studentService.getStudentSessions(
                userPrincipal.getUserId(),
                studentId,
                year,
                month,
                categoryId,
                status
        );
        return ApiResponse.success(SuccessCode.MONTHLY_SESSION_LIST_SUCCESS, sessions);
    }

    @Operation(
            summary = "세션 시퀀스별 통계 조회",
            description = "특정 세션의 시퀀스별 정답률 및 통계를 조회합니다. 시도 횟수가 적을수록 정답률이 높습니다."
    )
    @GetMapping("/dashboard/sequence-stats/{sessionId}")
    public ApiResponse<SessionSequenceStatsResponse> getSessionSequenceStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "세션 ID", example = "123")
            @PathVariable Long sessionId
    ) {
        SessionSequenceStatsResponse stats = studentService.getSessionSequenceStats(
                userPrincipal.getUserId(),
                sessionId
        );
        return ApiResponse.success(SuccessCode.SESSION_SEQUENCE_STATS_SUCCESS, stats);
    }

    @Operation(
            summary = "월별 달력 데이터 조회",
            description = "담당 학생들의 월별 일자별 세션 수 통계를 조회합니다. (Redis 캐시 적용)"
    )
    @GetMapping("/calendar/monthly")
    public ApiResponse<CalendarMonthlyResponse> getMonthlyCalendar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "조회 년도", example = "2025")
            @RequestParam int year,
            @Parameter(description = "조회 월 (1-12)", example = "1")
            @RequestParam int month
    ) {
        CalendarMonthlyResponse calendar = studentService.getMonthlyCalendar(
                userPrincipal.getUserId(),
                year,
                month
        );
        return ApiResponse.success(SuccessCode.CALENDAR_MONTHLY_SUCCESS, calendar);
    }

    @Operation(
            summary = "특정 날짜의 학생 세션 목록 조회",
            description = "특정 날짜에 특정 학생이 수행한 세션 목록을 조회합니다."
    )
    @GetMapping("/calendar/daily-sessions")
    public ApiResponse<DailySessionListResponse> getDailySessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "학생 ID", example = "1")
            @RequestParam Long studentId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailySessionListResponse sessions = studentService.getDailySessions(
                userPrincipal.getUserId(),
                studentId,
                date
        );
        return ApiResponse.success(SuccessCode.DAILY_SESSION_LIST_SUCCESS, sessions);
    }
}