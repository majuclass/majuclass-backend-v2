package com.ssafy.a202.global.security;


import com.ssafy.a202.global.constants.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * 개별 정보 추출 메서드만 제공
 * UserPrincipal에 대해 알 필요 없음
 */
@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // ================================
    // 토큰 생성 메서드들
    // ================================

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Long userId, String username, Role role) {
        return generateToken(userId, username, role, accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long userId, String username, Role role) {
        return generateToken(userId, username, role, refreshTokenExpiration);
    }

    /**
     * JWT 토큰 생성 (내부 구현)
     */
    private String generateToken(Long userId, String username, Role role, long expiration) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("jti", UUID.randomUUID().toString())  // JWT ID 추가
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public void validateToken(String token) throws JwtException {
        Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 토큰에서 역할 추출
     */
    public Role getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String role = claims.get("role", String.class); // String으로 읽기
        return Role.valueOf(role);  // String → Role enum 변환
    }

    /**
     * 토큰에서 발급 시간 추출
     */
    public Date getIssuedAtFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }

    /**
     * 토큰에서 JWT ID 추출
     */
    public String getJtiFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("jti", String.class);
    }

    /**
     * 토큰의 남은 만료 시간을 초 단위로 반환 (블랙리스트 TTL용)
     */
    public long getRemainingExpirationTimeInSeconds(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            long remainingMillis = Math.max(0, expiration.getTime() - now);
            return remainingMillis / 1000; // 밀리초를 초로 변환
        } catch (JwtException e) {
            log.debug("Failed to get remaining expiration time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 토큰에서 Claims 추출 - 예외를 그대로 던짐
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
