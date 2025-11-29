package com.ssafy.a202.domain.presignedUrl.dto.response;

public record S3PresignedUrlDto(
        int index,
        String fileName,
        String s3Key,
        String url,
        String operation,
        boolean success
) {
}
