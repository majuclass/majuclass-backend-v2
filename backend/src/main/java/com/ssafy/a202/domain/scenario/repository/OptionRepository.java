package com.ssafy.a202.domain.scenario.repository;

import com.ssafy.a202.domain.scenario.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findBySequenceIdAndDeletedAtIsNull(Long sequenceId);
}
