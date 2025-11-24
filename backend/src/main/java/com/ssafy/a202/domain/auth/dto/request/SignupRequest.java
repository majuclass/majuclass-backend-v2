package com.ssafy.a202.domain.auth.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SignupRequest(
        Long orgId,
        String username,
        String password,
        String fullName,
        String email
){
}
