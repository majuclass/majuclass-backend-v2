package com.ssafy.a202.domain.presignedUrl.dto.request;

import java.util.List;

public record S3PutPresignedUrlRequest(
        List<String> s3Keys,
        List<String> fileNames,
        String operation,
        String contentType
) {
}
