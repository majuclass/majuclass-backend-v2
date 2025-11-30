package com.ssafy.a202.domain.student.controller;

import com.ssafy.a202.common.annotation.UserId;
import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.student.dto.request.StudentRequest;
import com.ssafy.a202.domain.student.dto.response.StudentCreateResponse;
import com.ssafy.a202.domain.student.dto.response.StudentPreviewResponse;
import com.ssafy.a202.domain.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Student", description = "학생 관리 API")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "학생 생성", description = "새로운 학생을 등록합니다.")
    // todo: csv, excel 등 파일 일괄 등록 기능도 있음.
    @PostMapping
    public ResponseEntity<ApiResponse<StudentCreateResponse>> createStudent(
            @UserId Long userId,
            @RequestBody StudentRequest request
    ) {
        StudentCreateResponse response = studentService.create(userId, request);
        return ApiResponseEntity.created(
                "/api/students/" + response.studentId(),
                SuccessCode.STUDENT_CREATED_SUCCESS,
                response
        );
    }

    @Operation(summary = "학생 목록 조회", description = "선생님의 학생 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudentPreviewResponse>>> getStudents(
            @UserId Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<StudentPreviewResponse> response = studentService.getStudents(userId, pageable);
        return ApiResponseEntity.success(
                SuccessCode.STUDENT_GET_LIST_SUCCESS,
                response
        );
    }

    @Operation(summary = "학생 정보 수정", description = "기존 학생의 정보를 수정합니다.")
    @PutMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Void>> updateStudent(
            @UserId Long userId,
            @Parameter(description = "수정할 학생 ID", required = true, example = "1")
            @PathVariable Long studentId,
            @RequestBody StudentRequest request
    ) {
        studentService.update(userId, studentId, request);
        return ApiResponseEntity.success(
                SuccessCode.STUDENT_UPDATE_SUCCESS
        );
    }

    @Operation(summary = "학생 삭제", description = "학생을 삭제합니다.")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @UserId Long userId,
            @Parameter(description = "삭제할 학생 ID", required = true, example = "1")
            @PathVariable Long studentId
    ) {
        studentService.delete(userId, studentId);
        return ApiResponseEntity.success(
                SuccessCode.STUDENT_DELETE_SUCCESS
        );
    }
}
