package com.ssafy.a202.domain.user.entity;

import com.ssafy.a202.domain.school.entity.Classroom;
import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.global.constants.DisabilityType;
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
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Classroom classroom;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "disability_type")
    private DisabilityType disabilityType;

    @Column(length = 200)
    private String guardian_phone;

    @Builder
    public Student(School school, Teacher teacher, Classroom classroom,
                   String name, DisabilityType disabilityType, String guardian_phone) {
        this.school = school;
        this.teacher = teacher;
        this.classroom = classroom;
        this.name = name;
        this.disabilityType = disabilityType;
        this.guardian_phone = guardian_phone;
    }

    public void changeClassroom(Classroom newClassroom) {
        this.classroom = newClassroom;
    }
}
