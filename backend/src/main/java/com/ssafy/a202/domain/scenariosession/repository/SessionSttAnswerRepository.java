package com.ssafy.a202.domain.scenariosession.repository;

import com.ssafy.a202.domain.scenariosession.entity.SessionSttAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 세션 STT 답변 리포지토리
 */
@Repository
public interface SessionSttAnswerRepository extends JpaRepository<SessionSttAnswer, Long> {

    /**
     * 특정 세션의 모든 음성 답변 조회 (시퀀스 정보 포함)
     * 시퀀스 번호, 시도 번호 순으로 정렬
     */
    @Query("SELECT ssa FROM SessionSttAnswer ssa " +
            "JOIN FETCH ssa.scenarioSequence seq " +
            "WHERE ssa.scenarioSession.id = :sessionId " +
            "AND ssa.audioS3Key IS NOT NULL " +
            "ORDER BY seq.seqNo, ssa.attemptNo")
    List<SessionSttAnswer> findAllAudioAnswersBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 특정 세션의 모든 STT 답변을 시퀀스 정보와 함께 조회
     * 시퀀스 번호, 시도 번호 순으로 정렬
     */
    @Query("SELECT ssa FROM SessionSttAnswer ssa " +
            "JOIN FETCH ssa.scenarioSequence seq " +
            "WHERE ssa.scenarioSession.id = :sessionId " +
            "ORDER BY seq.seqNo, ssa.attemptNo")
    List<SessionSttAnswer> findAllBySessionIdWithSequence(@Param("sessionId") Long sessionId);
}