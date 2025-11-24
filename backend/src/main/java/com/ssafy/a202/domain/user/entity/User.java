package com.ssafy.a202.domain.user.entity;


import com.ssafy.a202.common.entity.BaseTimeEntity;
import com.ssafy.a202.domain.auth.dto.request.SignupRequest;
import com.ssafy.a202.domain.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public static User of(Organization org, SignupRequest request, String encodePassword) {
        return User.builder()
                .organization(org)
                .username(request.username())
                .password(encodePassword)
                .fullName(request.fullName())
                .email(request.email())
                .role(UserRole.TEACHER)
                .build();
    }
}
