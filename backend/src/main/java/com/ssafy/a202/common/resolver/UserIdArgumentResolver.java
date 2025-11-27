package com.ssafy.a202.common.resolver;

import com.ssafy.a202.common.annotation.UserId;
import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.common.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @UserId 어노테이션이 붙은 파라미터에 JWT 토큰의 사용자 ID를 자동 주입하는 Resolver
 */
@Slf4j
@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @UserId 어노테이션이 있고, 타입이 Long인 파라미터만 처리
        return parameter.hasParameterAnnotation(UserId.class) &&
               Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 사용자
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("User is not authenticated");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        // Principal이 UserPrincipal 타입이 아닌 경우
        if (!(principal instanceof UserPrincipal)) {
            log.debug("Principal is not UserPrincipal: {}", principal.getClass());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        Long userId = userPrincipal.getUserId();

        log.debug("Resolved userId from JWT: {}", userId);
        return userId;
    }
}