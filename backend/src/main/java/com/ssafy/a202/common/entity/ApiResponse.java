package com.ssafy.a202.common.entity;

public record ApiResponse<T>(
        ApiResponseStatus status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(ApiResponseStatus.SUCCESS, successCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiResponseStatus.SUCCESS, SuccessCode.SUCCESS_DEFAULT.getMessage(), data);
    }

    public static ApiResponse<Void> success(SuccessCode successCode) {
        return new ApiResponse<>(ApiResponseStatus.SUCCESS, successCode.getMessage(), null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ApiResponseStatus.SUCCESS, SuccessCode.SUCCESS_DEFAULT.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(ApiResponseStatus.ERROR, errorCode.getMessage(), data);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(ApiResponseStatus.ERROR, errorCode.getMessage(), null);
    }
}