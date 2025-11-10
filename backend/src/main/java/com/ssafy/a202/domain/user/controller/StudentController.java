package com.ssafy.a202.domain.user.controller;

import com.ssafy.a202.domain.user.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.user.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.user.dto.response.StudentResponse;
import com.ssafy.a202.domain.user.service.StudentService;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import com.ssafy.a202.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}