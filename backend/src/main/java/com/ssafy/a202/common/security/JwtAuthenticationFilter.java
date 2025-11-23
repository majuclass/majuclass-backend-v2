package com.ssafy.a202.common.security;

import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.entity.UserRole;
import com.ssafy.a202.domain.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                // 토큰 검증
                jwtProvider.validateToken(token);

                // 블랙리스트 확인
//                if (tokenBlacklistService.isBlacklisted(token)) {
//                    log.debug("Token is blacklisted (logged out)");
//                    SecurityContextHolder.clearContext();
//                    filterChain.doFilter(request, response);
//                    return;
//                }

                // 토큰 타입 확인 (access 토큰만 허용)
                String tokenType = jwtProvider.getTokenTypeFromToken(token);
                if (!"access".equals(tokenType)) {
                    log.debug("Invalid token type for API access: {}", tokenType);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                // 토큰에서 사용자 정보 추출
                Long userId = jwtProvider.getUserIdFromToken(token);
                String username = jwtProvider.getUsernameFromToken(token);
                UserRole role = jwtProvider.getRoleFromToken(token);

                // 사용자 삭제 여부 확인 (회원 탈퇴 또는 관리자에 의한 삭제)
                Optional<User> userOptional = userRepository.findById(userId);
                if (userOptional.isEmpty() || userOptional.get().isDeleted()) {
                    log.debug("User is deleted or not found: userId={}", userId);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                // UserPrincipal 생성
                UserPrincipal userPrincipal = new UserPrincipal(userId, username, role);

                // Authentication 객체 생성 및 SecurityContext에 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,  // principal
                                null,           // credentials
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful for user: {}", username);

            } catch (JwtException e) {
                log.debug("JWT validation failed: {}", e.getMessage());
                // 인증 실패 시 SecurityContext를 비워둠 (익명 사용자로 처리)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰 추출
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}