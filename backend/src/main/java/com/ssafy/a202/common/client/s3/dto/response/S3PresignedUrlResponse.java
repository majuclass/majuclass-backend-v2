package com.ssafy.a202.common.client.s3.dto.response;

import java.util.List;

public record S3PresignedUrlResponse(
        int total,
        int success,
        int failed,
        List<S3PresignedUrlDto> results
) {
}
