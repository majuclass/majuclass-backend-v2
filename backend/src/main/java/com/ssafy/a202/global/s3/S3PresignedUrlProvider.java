package com.ssafy.a202.global.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Slf4j
@Component
public class S3PresignedUrlProvider {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3PresignedUrlProvider(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    /**
     * S3 객체에 대한 프리사인드 URL 생성 (유효시간: 7일)
     *
     * @param s3Key S3 객체 키
     * @return 프리사인드 URL (GET 요청용)
     */
    public String generatePresignedUrl(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofDays(7))  // 최대 7일
                .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.debug("Generated presigned URL for key: {}", s3Key);
            return presignedUrl;
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", s3Key, e);
            throw new RuntimeException("S3 presigned URL 생성 실패", e);
        }
    }
}