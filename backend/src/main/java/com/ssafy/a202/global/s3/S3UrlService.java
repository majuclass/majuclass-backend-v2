package com.ssafy.a202.global.s3;

import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * S3 URL 생성 서비스
 * - scenarios/ (공개): Public URL 직접 생성
 * - students/ (비공개): Lambda를 통한 Pre-signed URL 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3UrlService {

    private final WebClient webClient;

    @Value("${aws.s3.public-url-base}")
    private String publicUrlBase;

    @Value("${aws.lambda.presigned-url-api}")
    private String lambdaApiUrl;

    /**
     * S3 URL 생성 (자동으로 Public/Pre-signed URL 선택)
     *
     * @param s3Key S3 객체 키
     * @return S3 접근 URL
     */
    public String generateUrl(String s3Key) {
        // scenarios로 시작하면 Public URL 반환
        if (s3Key.startsWith("scenarios/")) {
            return generatePublicUrl(s3Key);
        }
        // students로 시작하면 Pre-signed URL 반환
        else if (s3Key.startsWith("students/")) {
            return generatePresignedUrl(s3Key);
        }
        // 기타 경로는 Public URL로 처리 (기본값)
        else {
            log.warn("Unknown S3 key prefix: {}, using public URL", s3Key);
            return generatePublicUrl(s3Key);
        }
    }

    /**
     * Public URL 생성 (공개 객체)
     */
    private String generatePublicUrl(String s3Key) {
        String publicUrl = publicUrlBase + "/" + s3Key;
        log.debug("Generated public URL for key: {}", s3Key);
        return publicUrl;
    }

    /**
     * Lambda API를 통한 Pre-signed URL 생성 (비공개 객체 - 조회용)
     */
    private String generatePresignedUrl(String s3Key) {
        try {
            // Lambda API 호출 (조회용: getObject)
            Map<String, String> response = webClient.post()
                    .uri(lambdaApiUrl)
                    .bodyValue(Map.of(
                            "fileName", s3Key,
                            "operation", "getObject"
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();  // 동기 처리

            // 응답에서 Pre-signed URL 추출 (url 필드)
            if (response != null && response.containsKey("url")) {
                String presignedUrl = response.get("url");
                log.debug("Generated presigned URL via Lambda for fileName: {}", s3Key);
                return presignedUrl;
            } else {
                log.error("Invalid response from Lambda API for fileName: {}", s3Key);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
            }

        } catch (Exception e) {
            log.error("Failed to generate presigned URL via Lambda for fileName: {}", s3Key, e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    /**
     * 업로드용 Pre-signed URL 생성
     *
     * @param fileName S3 파일 경로
     * @param operation 작업 유형 (putObject)
     * @param contentType 파일 컨텐츠 타입
     * @return 업로드용 Pre-signed URL 응답 (url, fileName)
     */
    public Map<String, String> generateUploadPresignedUrl(String fileName, String operation, String contentType) {
        try {
            // Lambda API 호출 (업로드용)
            Map<String, String> response = webClient.post()
                    .uri(lambdaApiUrl)
                    .bodyValue(Map.of(
                            "fileName", fileName,
                            "operation", operation,
                            "contentType", contentType
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 응답 검증 (url과 fileName이 있어야 함)
            if (response != null && response.containsKey("url") && response.containsKey("fileName")) {
                log.debug("Generated upload presigned URL via Lambda for fileName: {}", fileName);
                return response;
            } else {
                log.error("Invalid response from Lambda API for upload URL, fileName: {}", fileName);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
            }

        } catch (Exception e) {
            log.error("Failed to generate upload presigned URL via Lambda for fileName: {}", fileName, e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
