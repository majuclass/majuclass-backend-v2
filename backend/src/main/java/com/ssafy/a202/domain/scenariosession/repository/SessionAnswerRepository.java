package com.ssafy.a202.domain.scenariosession.repository;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.domain.scenariosession.entity.SessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 세션 답변 리포지토리
 */
@Repository
public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, Long> {

    /**
     * 특정 세션의 특정 시퀀스에 대한 시도 횟수 조회
     */
    long countByScenarioSessionAndScenarioSequence(ScenarioSession scenarioSession, ScenarioSequence scenarioSequence);

    /**
     * 특정 세션의 모든 답변을 시퀀스 정보와 함께 조회
     * 시퀀스 번호, 시도 번호 순으로 정렬
     */
    @Query("SELECT sa FROM SessionAnswer sa " +
            "JOIN FETCH sa.scenarioSequence seq " +
            "WHERE sa.scenarioSession.id = :sessionId " +
            "ORDER BY seq.seqNo, sa.attemptNo")
    List<SessionAnswer> findAllBySessionIdWithSequence(@Param("sessionId") Long sessionId);

    /**
     * 특정 세션의 특정 시퀀스에 대한 모든 음성 답변 조회
     * 시도 번호 순으로 정렬
     */
    @Query("SELECT sa FROM SessionAnswer sa " +
            "JOIN FETCH sa.scenarioSequence seq " +
            "WHERE sa.scenarioSession.id = :sessionId " +
            "AND seq.seqNo = :sequenceNumber " +
            "AND sa.answerS3Key IS NOT NULL " +
            "ORDER BY sa.attemptNo")
    List<SessionAnswer> findAudioAnswersBySessionAndSequence(
            @Param("sessionId") Long sessionId,
            @Param("sequenceNumber") Integer sequenceNumber
    );
}