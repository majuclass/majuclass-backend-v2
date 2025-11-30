package com.ssafy.a202.domain.session.entity;

import com.ssafy.a202.domain.session.dto.request.AnswerCreateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private Long sequenceId;

    @Column(nullable = false)
    private int seqNo;

    @Column(nullable = false)
    private String seqQuestion;

    @Column(nullable = false)
    private int correctOptionNo;
    private String correctOptionText;
    private String correctOptionS3Key;

    @Column(nullable = false)
    private int selectedOptionNo;
    private String selectedOptionText;
    private String selectedOptionS3Key;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(nullable = false)
    private Integer attemptNo;

    private String audioS3Key;
    private String transcribedText;
    private double similarityScore;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public static Answer of(Session session, AnswerCreateRequest request) {
        return Answer.builder()
                .session(session)
                .sequenceId(request.sequenceId())
                .seqNo(request.seqNo())
                .seqQuestion(request.seqQuestion())
                .correctOptionNo(request.correctOptionNo())
                .correctOptionText(request.correctOptionText())
                .correctOptionS3Key(request.correctOptionS3Key())
                .selectedOptionNo(request.selectedOptionNo())
                .selectedOptionText(request.selectedOptionText())
                .selectedOptionS3Key(request.selectedOptionS3Key())
                .isCorrect(request.isCorrect())
                .attemptNo(request.attemptNo())
                .audioS3Key(request.audioS3Key())
                .transcribedText(request.transcribedText())
                .similarityScore(request.similarityScore())
                .build();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
