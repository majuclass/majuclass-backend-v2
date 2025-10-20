package com.ssafy.a202.domain.user.entity;

import com.ssafy.a202.domain.school.entity.Classroom;
import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Classroom classroom;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 300)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(length = 200)
    private String phone;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    @OneToMany(mappedBy = "teacher")
    private List<Student> students = new ArrayList<>();

    @Builder
    public Teacher(School school, Classroom classroom, String username, String password,
                   String name, String email, String phone, Boolean isAdmin) {
        this.school = school;
        this.classroom = classroom;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin != null ? isAdmin : false;
    }

    public void changeClassroom(Classroom newClassroom) {
        this.classroom = newClassroom;
    }
}