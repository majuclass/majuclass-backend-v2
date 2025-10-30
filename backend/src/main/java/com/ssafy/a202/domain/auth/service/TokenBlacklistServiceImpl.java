package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWT 토큰 블랙리스트 서비스 구현체
 * Redis를 사용하여 블랙리스트 토큰 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    /**
     * 토큰을 블랙리스트에 추가
     * JWT ID(jti)를 키로 사용하여 Redis에 저장하고, 토큰 만료 시간과 동일하게 TTL 설정
     */
    @Override
    public void addBlacklist(String token, long expirationSeconds) {
        try {
            String jti = jwtProvider.getJtiFromToken(token);
            String key = BLACKLIST_KEY_PREFIX + jti;
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofSeconds(expirationSeconds));
            log.info("Token JTI added to blacklist: {} with expiration: {}", jti, expirationSeconds);
        }  catch (Exception e) {
            log.warn("Failed to add token to blacklist, Redis maybe unavailable: {}", e.getMessage());
            // Redis가 사용 불가능한 경우에도 로그아웃 자체는 성공으로 처리
            // 토큰은 만료 시간이 되면 자동으로 무효화됨
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * JWT ID(jti)를 사용하여 블랙리스트 여부 확인
     */
    @Override
    public boolean isBlacklisted(String token) {
        try {
            String jti = jwtProvider.getJtiFromToken(token);
            String key = BLACKLIST_KEY_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        }  catch (Exception e) {
            log.error("Failed to check token blacklist status: ", e);
            // Redis 연결 실패 시 보수적으로 접근을 허용하지 않음
            return true;
        }
    }
}
