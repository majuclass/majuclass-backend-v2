package com.ssafy.a202.domain.user.repository;

import com.ssafy.a202.domain.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 학생 리포지토리
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * ID로 활성 학생 조회
     */
    Optional<Student> findByIdAndIsDeletedFalse(Long id);
}