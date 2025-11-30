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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

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

    @PutMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Void>> updateStudent(
            @UserId Long userId,
            @PathVariable String studentId,
            @RequestBody StudentRequest request
    ) {
        studentService.update(userId, studentId, request);
        return ApiResponseEntity.success(
                SuccessCode.STUDENT_UPDATE_SUCCESS
        );
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @UserId Long userId,
            @PathVariable String studentId
    ) {
        studentService.delete(userId, studentId);
        return ApiResponseEntity.success(
                SuccessCode.STUDENT_DELETE_SUCCESS
        );
    }
}
