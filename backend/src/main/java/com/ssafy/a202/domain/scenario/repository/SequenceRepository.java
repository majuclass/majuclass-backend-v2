package com.ssafy.a202.domain.scenario.repository;

import com.ssafy.a202.domain.scenario.entity.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    List<Sequence> findByScenarioIdAndDeletedAtIsNull(Long scenarioId);
}
