package com.ssafy.a202.domain.scenariosession.entity;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.global.constants.SessionStatus;
import com.ssafy.a202.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "scenario_sessions")
public class ScenarioSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "scenarioSession", cascade = CascadeType.ALL)
    private List<SessionAnswer> sessionAnswers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus sessionStatus;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;
}