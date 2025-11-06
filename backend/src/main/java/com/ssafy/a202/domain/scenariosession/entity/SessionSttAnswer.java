package com.ssafy.a202.domain.scenariosession.entity;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "session_stt_answers")
public class SessionSttAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ScenarioSession scenarioSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_id", nullable = false)
    private ScenarioSequence scenarioSequence;

    @Column(name = "audio_s3_key")
    private String audioS3Key;

    @Column(nullable = false, name = "transcribed_text")
    private String transcribedText;

    @Column(nullable = false, name = "answer_text")
    private String answerText;

    @Column(nullable = false, name = "similarity_score")
    private Double similarityScore;

    @Column(nullable = false, name = "is_correct", columnDefinition = "TINYINT(1)")
    private boolean isCorrect;

    @Column(nullable = false, name = "attempt_no")
    private int attemptNo;
}
