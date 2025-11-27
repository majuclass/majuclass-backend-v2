package com.ssafy.a202.domain.user.repository;

import com.ssafy.a202.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long userId);
}
