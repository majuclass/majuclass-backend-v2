package com.ssafy.a202.domain.user.entity;

import com.ssafy.a202.domain.school.entity.Classroom;
import com.ssafy.a202.domain.school.entity.School;
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

    @Column(length = 200)
    private String phone;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    @OneToMany(mappedBy = "user")
    private List<Student> students = new ArrayList<>();

    @Builder
    public User(School school, String username, String password,
                   String name, String email, String phone, Boolean isAdmin) {
        this.school = school;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin != null ? isAdmin : false;
    }
}