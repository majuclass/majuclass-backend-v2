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
@Table(name = "session_answer")
public class SessionAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ScenarioSession scenarioSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_id")
    private ScenarioSequence scenarioSequence;

    @Column(nullable = false)
    private String audioS3Bucket;

    @Column(nullable = false)
    private String audioS3Key;

    @Column(nullable = false)
    private Integer attemptNo;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isCorrectAnswer;
}