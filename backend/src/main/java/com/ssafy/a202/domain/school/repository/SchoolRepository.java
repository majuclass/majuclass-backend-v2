package com.ssafy.a202.domain.school.repository;

import com.ssafy.a202.domain.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}