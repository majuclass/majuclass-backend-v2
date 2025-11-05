package com.ssafy.a202.domain.scenariosession.repository;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.domain.scenariosession.entity.SessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 세션 답변 리포지토리
 */
@Repository
public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, Long> {

    /**
     * 특정 세션의 특정 시퀀스에 대한 시도 횟수 조회
     */
    long countByScenarioSessionAndScenarioSequence(ScenarioSession scenarioSession, ScenarioSequence scenarioSequence);
}