package com.ssafy.a202.common.security;


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
        return generateToken(userId, username, role, "access", accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long userId, String username, Role role) {
        return generateToken(userId, username, role, "refresh", refreshTokenExpiration);
    }

    /**
     * JWT 토큰 생성 (내부 구현)
     * HS256 (HMAC-SHA256) 알고리즘 명시적 사용
     */
    private String generateToken(Long userId, String username, Role role, String tokenType, long expiration) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("jti", UUID.randomUUID().toString())  // JWT ID 추가
                .claim("token_type", tokenType)
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        }  catch (JwtException e) {
            // 예외를 그대로 던져서 Filter에서 처리하게 함
            log.debug("JWT validation failed: {}", e.getMessage());
            throw e;
        }
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
     * 토큰에서 토큰 타입 추출
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("token_type", String.class);
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
