package com.ssafy.a202.domain.scenario.entity;

import com.ssafy.a202.common.entity.BaseTimeEntity;
import com.ssafy.a202.domain.scenario.dto.request.SequenceRequest;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "sequences")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sequence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @Column(nullable = false)
    private Integer seqNo;

    @Column(nullable = false)
    private String question;

    public static Sequence from(Scenario scenario, SequenceRequest seq) {
        return Sequence.builder()
                .scenario(scenario)
                .seqNo(seq.seqNo())
                .question(seq.question())
                .build();
    }
}
