package com.ssafy.a202.domain.user.repository;

import com.ssafy.a202.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * username으로 사용자 조회
     */
    Optional<User> findByUsername(String username);

    /**
     * username 존재 여부 확인
     */
    boolean existsByUsername(String username);
}