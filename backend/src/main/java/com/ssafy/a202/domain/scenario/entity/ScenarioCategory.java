package com.ssafy.a202.domain.scenario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "scenario_categories")
public class ScenarioCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "scenarioCategory")
    private List<Scenario> scenarios = new ArrayList<>();

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
}
