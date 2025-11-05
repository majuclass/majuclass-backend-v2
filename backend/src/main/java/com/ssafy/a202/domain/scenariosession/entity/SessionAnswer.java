package com.ssafy.a202.domain.scenariosession.entity;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 세션 답변 엔티티
 * - 모든 시도(정답/오답) 기록
 * - 시도 횟수는 서버에서 자동 계산
 * - 상 난이도: 음성 답변 S3 키 추가 저장
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "session_answers")
public class SessionAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ScenarioSession scenarioSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_id", nullable = false)
    private ScenarioSequence scenarioSequence;

    @Column(name = "answer_s3_key")
    private String answerS3Key;

    @Column(name = "is_correct", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isCorrect;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo;
}