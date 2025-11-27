package com.ssafy.a202.domain.scenario.repository;

import com.ssafy.a202.domain.scenario.entity.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceRepository extends JpaRepository<Sequence, Long> {
}
