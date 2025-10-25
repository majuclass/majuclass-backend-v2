package com.ssafy.a202.domain.scenario.repository;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    /**
     * 삭제되지 않은 모든 시나리오 조회
     */
    List<Scenario> findByIsDeletedFalse();

    /**
     * 카테고리별 시나리오 조회
     */
    List<Scenario> findByScenarioCategoryIdAndIsDeletedFalse(Long categoryId);
}