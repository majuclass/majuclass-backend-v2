package com.ssafy.a202.common.client.s3.dto.request;

import java.util.List;

public record S3PutPresignedUrlRequest(
        List<String> s3Keys,
        String operation,
        String contentType
) {
}
