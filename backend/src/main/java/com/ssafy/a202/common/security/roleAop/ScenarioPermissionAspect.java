package com.ssafy.a202.common.security.roleAop;

import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.entity.UserRole;
import com.ssafy.a202.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioPermissionAspect {

    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;

    @Before("@annotation(checkPermission)")
    public void checkPermission(JoinPoint joinPoint, CheckScenarioPermission checkPermission) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        Long scenarioId = (Long) args[1];

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Scenario scenario = scenarioRepository.findByIdAndDeletedAtIsNull(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        PermissionAction action = checkPermission.value();

        if (!hasPermission(user, scenario, action)) {
            log.warn("Permission denied - userId: {}, scenarioId: {}, action: {}, userRole: {}",
                    userId, scenarioId, action, user.getRole());
            throw new CustomException(ErrorCode.SCENARIO_UPDATE_FORBIDDEN);
        }

        log.info("Permission granted - userId: {}, scenarioId: {}, action: {}, userRole: {}",
                userId, scenarioId, action, user.getRole());
    }

    private boolean hasPermission(User user, Scenario scenario, PermissionAction action) {
        return switch (action) {
            case UPDATE -> canUpdate(user, scenario);
            case DELETE -> canDelete(user, scenario);
            case VIEW -> canView(user, scenario);
        };
    }

    private boolean canUpdate(User user, Scenario scenario) {
        UserRole userRole = user.getRole();

        // ADMIN은 모든 시나리오 수정 가능
        if (userRole == UserRole.ADMIN) {
            return true;
        }

        // TEACHER는 본인이 생성한 시나리오만 수정 가능
        if (userRole == UserRole.TEACHER) {
            return scenario.getUser().getId().equals(user.getId());
        }

        // ORG_ADMIN은 같은 기관 소속이 생성한 시나리오 모두 수정 가능
        if (userRole == UserRole.ORG_ADMIN) {
            User scenarioCreator = scenario.getUser();

            return scenarioCreator.getOrganization().getId()
                    .equals(user.getOrganization().getId());
        }

        return false;
    }

    private boolean canDelete(User user, Scenario scenario) {
        // DELETE 권한 로직 (향후 필요시 구현)
        // ADMIN만 삭제 가능
        return user.getRole() == UserRole.ADMIN;
    }

    private boolean canView(User user, Scenario scenario) {
        // VIEW 권한 로직 (향후 필요시 구현)
        // 모든 사용자가 조회 가능
        return true;
    }
}