package com.ssafy.a202.domain.scenario.entity;

import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "seq_options")
public class seqOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seq_id")
    private ScenarioSequence scenarioSequence;

    @Column(nullable = false)
    private int optionNo;

    @Column(nullable = false)
    private String optionText;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isAnswer;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;
}
