package com.ssafy.a202.domain.user.repository;

import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ================================
    // 1. JWT 기반 인증용 메서드
    // ================================

    /**
     * ID로 활성 사용자 조회 (JWT 토큰에서 ID 추출 후 사용)
     */
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    /**
     * 로그인 시 username으로 사용자 조회 (최초 인증용)
     */
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    // ================================
    // 2. 사용자 관리용 메서드
    // ================================

    /**
     * 사용자명 중복 확인 (회원가입 시)
     */
    boolean existsByUsernameAndIsDeletedFalse(String username);

    /**
     * 이메일 중복 확인 (회원가입 시)
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * 학교별 활성 사용자 목록 조회
     */
    List<User> findBySchoolAndIsDeletedFalse(School school);
}