package com.ssafy.a202.domain.presignedUrl.dto.request;

import java.util.List;

public record S3GetPresignedUrlRequest(
        List<String> s3Keys,
        String operation
) {
}
