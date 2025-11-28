package com.ssafy.a202.common.client.s3;

import com.ssafy.a202.common.client.s3.dto.request.S3GetPresignedUrlRequest;
import com.ssafy.a202.common.client.s3.dto.request.S3PutPresignedUrlRequest;
import com.ssafy.a202.common.client.s3.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class S3Client {

    private final WebClient webClient;

    @Value("${aws.lambda.presigned-url-api}")
    private String apiGatewayUrl;

    @Value("${aws.s3.public-url-base}")
    private String publicBucket;

    public S3PresignedUrlResponse getPresignedUrl(S3GetPresignedUrlRequest request) {
        return webClient
                .post()
                .uri(apiGatewayUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(S3PresignedUrlResponse.class) // JSON → DTO 변환
                .block(); // 동기방식으로 결과 대기
    }

    public S3PresignedUrlResponse getPresignedUrl(S3PutPresignedUrlRequest request) {
        return webClient
                .post()
                .uri(apiGatewayUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(S3PresignedUrlResponse.class)
                .block();
    }

    public String getPublicS3Url(String s3Key) {
        return publicBucket + "/" + s3Key;
    }
}
