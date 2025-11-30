package com.ssafy.a202.domain.student.entity;


import com.ssafy.a202.common.entity.BaseTimeEntity;
import com.ssafy.a202.domain.organization.entity.Organization;
import com.ssafy.a202.domain.student.dto.request.StudentRequest;
import com.ssafy.a202.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fullName;

    public static Student of(User homeroomTeacher, StudentRequest request) {
        return Student.builder()
                .organization(homeroomTeacher.getOrganization())
                .user(homeroomTeacher)
                .fullName(request.fullName())
                .build();
    }

    public void update(User homeroomTeacher, StudentRequest request) {
        if (request.fullName() != null) {
            this.fullName = request.fullName();
        }
        if (homeroomTeacher != null) {
            this.user = homeroomTeacher;
        }
    }

}
