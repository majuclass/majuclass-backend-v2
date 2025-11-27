package com.ssafy.a202.common.entity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

public record ApiResponseEntity<T>(
        ResponseEntity<ApiResponse<T>> entity
) {

    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(successCode, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(data));
    }

    public static ResponseEntity<ApiResponse<Void>> success(SuccessCode successCode) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(successCode));
    }

    public static ResponseEntity<ApiResponse<Void>> success() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success());
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String location, SuccessCode successCode, T data) {
        return ResponseEntity
                .created(URI.create(location))
                .body(ApiResponse.success(successCode, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String location, T data) {
        return ResponseEntity
                .created(URI.create(location))
                .body(ApiResponse.success(data));
    }
}