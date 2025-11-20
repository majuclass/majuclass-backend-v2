package com.ssafy.a202.domain.scenario.repository;

import com.ssafy.a202.domain.scenario.entity.ScenarioCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 시나리오 카테고리 Repository
 */
@Repository
public interface ScenarioCategoryRepository extends JpaRepository<ScenarioCategory, Long> {
}