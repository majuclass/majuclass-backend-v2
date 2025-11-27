package com.ssafy.a202.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT 토큰에서 사용자 ID를 자동으로 추출하는 커스텀 어노테이션
 *
 * 사용 예시:
 * @PostMapping
 * public ResponseEntity<?> create(@UserId Long userId, @RequestBody Request request) {
 *     // userId는 JWT 토큰에서 자동으로 추출됨
 * }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserId {
}