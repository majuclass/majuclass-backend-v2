package com.ssafy.a202.domain.scenario.entity;

import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "seq_options")
public class SeqOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_id")
    private ScenarioSequence scenarioSequence;

    @Column(name = "option_no", nullable = false)
    private int optionNo;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "option_s3_key", nullable = false)
    private String optionS3Key;

    @Column(name = "is_answer", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isAnswer;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;
}
