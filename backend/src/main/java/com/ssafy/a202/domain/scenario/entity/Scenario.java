package com.ssafy.a202.domain.scenario.entity;

import com.ssafy.a202.common.entity.BaseTimeEntity;
import com.ssafy.a202.domain.category.entity.Category;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioCreateRequest;
import com.ssafy.a202.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "scenarios")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scenario extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private String thumbnailS3Key;

    private String backgroundS3Key;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    public static Scenario from(User user, Category category, ScenarioCreateRequest request) {
        return Scenario.builder()
                .user(user)
                .category(category)
                .title(request.title())
                .description(request.description())
                .thumbnailS3Key(request.thumbnailS3Key())
                .backgroundS3Key(request.backgroundS3Key())
                .difficultyLevel(request.difficultyLevel())
                .build();
    }
}
