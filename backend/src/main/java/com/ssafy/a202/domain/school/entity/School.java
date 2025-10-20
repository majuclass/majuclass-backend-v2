package com.ssafy.a202.domain.school.entity;

import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.domain.user.entity.Teacher;
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

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Classroom> classrooms = new ArrayList<>();

    @OneToMany(mappedBy = "school")
    private List<Teacher> teachers = new ArrayList<>();

    @OneToMany(mappedBy = "school")
    private List<Student> students = new ArrayList<>();

    @Builder
    public School(String schoolName) {
        this.schoolName = schoolName;
    }
}
