package com.ssafy.a202.domain.presignedUrl.controller;

import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.presignedUrl.dto.request.PresignedRequest;
import com.ssafy.a202.domain.presignedUrl.dto.response.S3PresignedUrlResponse;
import com.ssafy.a202.domain.presignedUrl.service.PresignedUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/presigned-urls")
@RequiredArgsConstructor
@Tag(name = "Presigned URL", description = "S3 Presigned URL 생성 API")
public class PresignedUrlController {

    private final PresignedUrlService presignedUrlService;

    @Operation(summary = "S3 업로드용 Presigned URL 생성", description = "파일 업로드를 위한 S3 Presigned URL을 생성합니다.")
    @PostMapping("/put")
    public ResponseEntity<ApiResponse<S3PresignedUrlResponse>> getPutPresignedUrl(
            @RequestBody PresignedRequest request
    ) {
        S3PresignedUrlResponse response = presignedUrlService.generatePutPresignedUrls(request);
        return ApiResponseEntity.success(
                SuccessCode.PRESIGNED_URL_GET_PUT_SUCCESS,
                response
        );
    }
}
