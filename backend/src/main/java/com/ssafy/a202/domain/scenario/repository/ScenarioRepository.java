package com.ssafy.a202.domain.scenario.repository;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

}
