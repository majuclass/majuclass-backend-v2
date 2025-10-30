package com.ssafy.a202.domain.auth.service;

public interface TokenBlacklistService {

    /**
     * 토큰을 블랙리스트에 추가
     * @param token JWT 토큰
     * @param expirationSeconds 토큰 만료 시간 (초)
     */
    void addBlacklist(String token, long expirationSeconds);

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    boolean isBlacklisted(String token);
}
