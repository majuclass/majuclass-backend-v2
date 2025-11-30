package com.ssafy.a202.domain.session.entity;

import com.ssafy.a202.common.entity.BaseTimeEntity;
import com.ssafy.a202.domain.session.dto.request.SessionStartRequest;
import com.ssafy.a202.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sessions")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long scenarioId;

    @Column(nullable = false)
    private String stdFullName;

    @Column(nullable = false)
    private String scnTitle;

    @Column(nullable = false)
    private String scnDescription;

    private String scnThumbnailS3Key;

    private String scnBackgroundS3Key;

    @Column(nullable = false)
    private String scnDifficultyLevel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus;

    public void finish() {
        this.sessionStatus = SessionStatus.COMPLETED;
    }

    public void abort() {
        this.sessionStatus = SessionStatus.ABORTED;
    }

    public static Session of(Student student, SessionStartRequest request) {
        return Session.builder()
                .scenarioId(request.scenarioId())
                .studentId(student.getId())
                .stdFullName(student.getFullName())
                .scnTitle(request.scenarioSnapshot().scnTitle())
                .scnDescription(request.scenarioSnapshot().scnDescription())
                .scnThumbnailS3Key(request.scenarioSnapshot().scnThumbnailS3Key())
                .scnBackgroundS3Key(request.scenarioSnapshot().scnBackgroundS3Key())
                .scnDifficultyLevel(request.scenarioSnapshot().scnDifficultyLevel())
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .build();
    }
}
