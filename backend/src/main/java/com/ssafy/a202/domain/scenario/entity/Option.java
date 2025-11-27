package com.ssafy.a202.domain.scenario.entity;


import com.ssafy.a202.common.entity.BaseTimeEntity;
import com.ssafy.a202.domain.scenario.dto.request.OptionRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "options")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id", nullable = false)
    private Sequence sequence;

    @Column(nullable = false)
    private Integer optionNo;

    @Column(nullable = false)
    private boolean isCorrect;

    private String optionText;

    private String optionS3Key;

    public static Option from(Sequence sequence, OptionRequest opt) {
        return Option.builder()
                .sequence(sequence)
                .optionNo(opt.optionNo())
                .isCorrect(opt.isCorrect())
                .optionText(opt.optionText())
                .optionS3Key(opt.optionS3Key())
                .build();
    }
}


