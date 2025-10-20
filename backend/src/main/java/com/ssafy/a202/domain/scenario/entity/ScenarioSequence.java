package com.ssafy.a202.domain.scenario.entity;

import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "scenario_sequences")
public class ScenarioSequence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "scenarioSequence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeqOption> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(nullable = false)
    private int seqNo;

    @Column(nullable = false)
    private String seqS3Bucket;

    @Column(nullable = false)
    private String seqS3Key;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;
}
