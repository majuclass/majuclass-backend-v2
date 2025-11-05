package com.ssafy.a202.domain.user.entity;

import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.global.constants.Role;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();

    @Builder
    public User(School school, String username, String password, String name, String email, Role role) {
        this.school = school;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role != null ? role : Role.USER;
    }

    // ================================
    // 연관관계 편의 메서드
    // ================================

    /**
     * 학생을 담당 목록에 추가
     * 양방향 관계를 안전하게 설정
     */
    public void addStudent(Student student) {
        this.students.add(student);
    }

    /**
     * 학생을 담당 목록에서 제거
     */
    public void removeStudent(Student student) {
        this.students.remove(student);
    }
}