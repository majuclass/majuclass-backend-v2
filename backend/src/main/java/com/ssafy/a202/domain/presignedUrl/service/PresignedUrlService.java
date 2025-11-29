package com.ssafy.a202.domain.presignedUrl.service;

import com.ssafy.a202.domain.presignedUrl.client.S3Client;
import com.ssafy.a202.domain.presignedUrl.dto.request.PresignedRequest;
import com.ssafy.a202.domain.presignedUrl.dto.request.S3PutPresignedUrlRequest;
import com.ssafy.a202.domain.presignedUrl.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Client s3Client;

    public S3PresignedUrlResponse generatePutPresignedUrls(PresignedRequest request) {
        List<String> s3Keys = new ArrayList<>();

        for (String fileName : request.fileNames()) {
            String s3Key = generateS3Key(request.domain(), fileName);
            s3Keys.add(s3Key);
        }

        S3PutPresignedUrlRequest s3Request = new S3PutPresignedUrlRequest(
                s3Keys,
                request.fileNames(),
                "putObject",
                request.contentType()
        );

        return s3Client.getS3PutPresignedUrl(s3Request);
    }

    private String generateS3Key(String domain, String fileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = extractExtension(fileName);

        if (extension.isEmpty()) {
            return domain + "/" + uuid;
        } else {
            return domain + "/" + uuid + "." + extension;
        }
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }
}