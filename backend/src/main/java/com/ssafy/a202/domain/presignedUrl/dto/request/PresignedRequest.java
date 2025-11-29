package com.ssafy.a202.domain.presignedUrl.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PresignedRequest(
        List<String> fileNames,
        String contentType,
        String domain
) {
}
