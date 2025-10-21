package com.ssafy.a202.domain.school.entity;

import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_name", nullable = false, length = 50)
    private String schoolName;

    @OneToMany(mappedBy = "school")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "school")
    private List<Student> students = new ArrayList<>();

    @Builder
    public School(String schoolName) {
        this.schoolName = schoolName;
    }

    // ================================
    // 연관관계 편의 메서드
    // ================================

    /**
     * 학교에 선생님 추가
     */
    public void addUser(User user) {
        this.users.add(user);
    }

    /**
     * 학교에 학생 추가
     */
    public void addStudent(Student student) {
        this.students.add(student);
    }
}
