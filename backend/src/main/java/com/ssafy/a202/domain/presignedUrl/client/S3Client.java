package com.ssafy.a202.domain.presignedUrl.client;

import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.domain.presignedUrl.dto.request.S3GetPresignedUrlRequest;
import com.ssafy.a202.domain.presignedUrl.dto.request.S3PutPresignedUrlRequest;
import com.ssafy.a202.domain.presignedUrl.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Client {

    private final WebClient webClient;

    @Value("${aws.lambda.presigned-url-api}")
    private String apiGatewayUrl;

    @Value("${aws.s3.public-url-base}")
    private String publicBucket;

    public S3PresignedUrlResponse getS3GetPresignedUrl(S3GetPresignedUrlRequest request) {
        return webClient
                .post()
                .uri(apiGatewayUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(S3PresignedUrlResponse.class) // JSON → DTO 변환
                .block(); // 동기방식으로 결과 대기
    }

    public S3PresignedUrlResponse getS3PutPresignedUrl(S3PutPresignedUrlRequest request) {
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

    /**
     * S3에 파일이 존재하는지 확인 (HTTP HEAD 요청)
     */
    public boolean existsS3File(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return false;
        }

        try {
            String url = getPublicS3Url(s3Key);

            webClient
                .head()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .block();

            return true;
        } catch (Exception e) {
            log.debug("S3 file not found: {}", s3Key);
            return false;
        }
    }

    /**
     * S3 파일 존재 여부 검증 (예외 발생)
     */
    public void validateS3FileExists(String s3Key) {
        if (!existsS3File(s3Key)) {
            log.warn("S3 파일 검증 실패: {}", s3Key);
            throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
        }
    }
}
