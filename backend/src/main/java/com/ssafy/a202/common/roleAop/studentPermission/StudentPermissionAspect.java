package com.ssafy.a202.common.roleAop.studentPermission;

import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.common.roleAop.PermissionAction;
import com.ssafy.a202.domain.student.entity.Student;
import com.ssafy.a202.domain.student.repository.StudentRepository;
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
public class StudentPermissionAspect {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Before("@annotation(checkPermission)")
    public void checkPermission(JoinPoint joinPoint, CheckStudentPermission checkPermission) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String studentId = (String) args[1];

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Student student = studentRepository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        PermissionAction action = checkPermission.value();

        if (!hasPermission(user, student, action)) {
            log.warn("Permission denied - userId: {}, studentId: {}, action: {}, userRole: {}",
                    userId, studentId, action, user.getRole());
            throw new CustomException(ErrorCode.STUDENT_PERMISSION_DENIED);
        }

        log.info("Permission granted - userId: {}, studentId: {}, action: {}, userRole: {}",
                userId, studentId, action, user.getRole());
    }

    private boolean hasPermission(User user, Student student, PermissionAction action) {
        return switch (action) {
            case UPDATE -> canUpdate(user, student);
            case DELETE -> canDelete(user, student);
            case VIEW -> canView(user, student);
        };
    }

    private boolean canUpdate(User user, Student student) {
        UserRole userRole = user.getRole();

        // ADMIN은 모든 학생 수정 가능
        if (userRole == UserRole.ADMIN) {
            return true;
        }

        // ORG_ADMIN은 같은 기관 학생만 수정 가능
        if (userRole == UserRole.ORG_ADMIN) {
            return student.getOrganization().getId()
                    .equals(user.getOrganization().getId());
        }

        // TEACHER는 자기 담당 학생만 수정 가능
        if (userRole == UserRole.TEACHER) {
            return student.getUser().getId().equals(user.getId());
        }

        return false;
    }

    private boolean canDelete(User user, Student student) {
        UserRole userRole = user.getRole();

        // ADMIN은 모든 학생 삭제 가능
        if (userRole == UserRole.ADMIN) {
            return true;
        }

        // ORG_ADMIN은 같은 기관 학생만 삭제 가능
        if (userRole == UserRole.ORG_ADMIN) {
            return student.getOrganization().getId()
                    .equals(user.getOrganization().getId());
        }

        // TEACHER는 자기 담당 학생만 삭제 가능
        if (userRole == UserRole.TEACHER) {
            return student.getUser().getId().equals(user.getId());
        }

        return false;
    }

    private boolean canView(User user, Student student) {
        // VIEW 권한 로직 (필요시 구현)
        // 기본적으로 모든 사용자가 조회 가능
        return true;
    }
}