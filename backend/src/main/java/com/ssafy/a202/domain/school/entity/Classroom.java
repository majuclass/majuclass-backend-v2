package com.ssafy.a202.domain.school.entity;

import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.domain.user.entity.Teacher;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Classroom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "class_grade", nullable = false, length = 50)
    private String classGrade;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @OneToMany(mappedBy = "classroom")
    private List<Teacher> teachers = new ArrayList<>();

    @OneToMany(mappedBy = "classroom")
    private List<Student> students = new ArrayList<>();

    @Builder
    public Classroom(School school, String classGrade, String className) {
        this.school = school;
        this.classGrade = classGrade;
        this.className = className;
    }

    public void updateInfo(String classGrade, String className) {
        this.classGrade = classGrade;
        this.className = className;
    }
}
