package com.ssafy.a202.domain.session.repository;

import com.ssafy.a202.domain.session.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
