package com.ssafy.a202.domain.session.repository;

import com.ssafy.a202.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByIdAndDeletedAtIsNull(Long sessionId);
}
