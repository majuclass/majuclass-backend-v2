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
@Table(name = "scenarios")
public class Scenario extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScenarioSequence> scenarioSequences = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_category_id")
    private ScenarioCategory scenarioCategory;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Column(name = "thumbnail_s3_key", nullable = false)
    private String thumbnailS3Key;

    @Column(name = "background_s3_key", nullable = false)
    private String backgroundS3Key;

    @Column(name = "total_sequences", nullable = false)
    private int totalSequences;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    /**
     * 시나리오 기본 정보 수정
     */
    public void updateBasicInfo(String title, String summary, ScenarioCategory category) {
        this.title = title;
        this.summary = summary;
        this.scenarioCategory = category;
    }

    /**
     * 썸네일 이미지 수정
     */
    public void updateThumbnail(String thumbnailS3Key) {
        this.thumbnailS3Key = thumbnailS3Key;
    }

    /**
     * 배경 이미지 수정
     */
    public void updateBackground(String backgroundS3Key) {
        this.backgroundS3Key = backgroundS3Key;
    }

    /**
     * 총 시퀀스 개수 업데이트
     */
    public void updateTotalSequences(int totalSequences) {
        this.totalSequences = totalSequences;
    }

    /**
     * 시나리오 삭제 (soft delete)
     */
    public void delete() {
        this.isDeleted = true;
    }
}
