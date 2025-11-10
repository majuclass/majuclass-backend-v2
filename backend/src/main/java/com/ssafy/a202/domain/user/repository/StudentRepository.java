package com.ssafy.a202.domain.user.repository;

import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * 특정 선생님이 담당하는 활성 학생 목록 조회
     */
    List<Student> findByUserAndIsDeletedFalse(User user);

    /**
     * 특정 학교의 활성 학생 목록 조회
     */
    List<Student> findBySchoolAndIsDeletedFalse(School school);
}