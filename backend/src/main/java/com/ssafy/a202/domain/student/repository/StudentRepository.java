package com.ssafy.a202.domain.student.repository;

import com.ssafy.a202.domain.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Page<Student> findByDeletedAtIsNull(Pageable pageable);

    Page<Student> findByOrganizationIdAndDeletedAtIsNull(Long organizationId, Pageable pageable);

    Page<Student> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<Student> findByIdAndDeletedAtIsNull(Long studentId);
}
