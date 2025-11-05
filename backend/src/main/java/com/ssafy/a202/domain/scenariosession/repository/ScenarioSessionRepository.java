package com.ssafy.a202.domain.scenariosession.repository;

import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 시나리오 세션 리포지토리
 */
@Repository
public interface ScenarioSessionRepository extends JpaRepository<ScenarioSession, Long> {
}