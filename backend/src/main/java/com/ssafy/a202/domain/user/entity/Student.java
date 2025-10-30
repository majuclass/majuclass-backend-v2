package com.ssafy.a202.domain.user.entity;

import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;

    @Builder
    public Student(School school, User user, String name) {
        this.school = school;
        this.name = name;
        if (user != null) {
            setUser(user);
        }
    }

    // ================================
    // 연관관계 편의 메서드
    // ================================

    /**
     * 담당 선생님 설정
     * 양방향 관계를 안전하게 설정
     */
    public void setUser(User user) {
        // 기존 선생님과의 관계 제거
        if (this.user != null) {
            this.user.removeStudent(this);
        }

        // 새로운 선생님 설정
        this.user = user;

        // 양방향 관계 설정
        if (user != null && !user.getStudents().contains(this)) {
            user.addStudent(this);
        }
    }
}
