package com.ssafy.a202.common.client.s3.dto.response;

public record S3PresignedUrlDto(
        int index,
        String s3Key,
        String url,
        String operation,
        boolean success
) {
}
