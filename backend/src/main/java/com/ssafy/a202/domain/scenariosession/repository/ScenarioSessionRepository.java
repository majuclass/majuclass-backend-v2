package com.ssafy.a202.domain.scenariosession.repository;

import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.global.constants.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시나리오 세션 리포지토리
 */
@Repository
public interface ScenarioSessionRepository extends JpaRepository<ScenarioSession, Long> {

    /**
     * 특정 학생의 특정 월의 세션 목록 조회 (카테고리, 상태 필터링 옵션)
     */
    @Query("SELECT ss FROM ScenarioSession ss " +
            "JOIN FETCH ss.scenario s " +
            "JOIN FETCH s.scenarioCategory sc " +
            "WHERE ss.student.id = :studentId " +
            "AND ss.createdAt >= :startDate " +
            "AND ss.createdAt < :endDate " +
            "AND ss.isDeleted = false " +
            "AND (:categoryId IS NULL OR sc.id = :categoryId) " +
            "AND (:status IS NULL OR ss.sessionStatus = :status) " +
            "ORDER BY ss.createdAt DESC")
    List<ScenarioSession> findMonthlySessionsByStudent(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("categoryId") Long categoryId,
            @Param("status") SessionStatus status
    );

    /**
     * 특정 학생의 특정 월의 카테고리별 세션 개수 조회
     */
    @Query("SELECT sc.id, sc.categoryName, COUNT(ss) " +
            "FROM ScenarioSession ss " +
            "JOIN ss.scenario s " +
            "JOIN s.scenarioCategory sc " +
            "WHERE ss.student.id = :studentId " +
            "AND ss.createdAt >= :startDate " +
            "AND ss.createdAt < :endDate " +
            "AND ss.isDeleted = false " +
            "GROUP BY sc.id, sc.categoryName")
    List<Object[]> findCategoryStatsByStudent(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 유저가 담당하는 학생들의 월별 일자별 세션 수 집계
     * 결과: [날짜, 학생ID, 학생이름, 세션개수]
     */
    @Query("SELECT DATE(ss.createdAt), st.id, st.name, COUNT(ss) " +
            "FROM ScenarioSession ss " +
            "JOIN ss.student st " +
            "WHERE st.user.id = :userId " +
            "AND st.isDeleted = false " +
            "AND ss.createdAt >= :startDate " +
            "AND ss.createdAt < :endDate " +
            "AND ss.isDeleted = false " +
            "GROUP BY DATE(ss.createdAt), st.id, st.name " +
            "ORDER BY DATE(ss.createdAt), st.name")
    List<Object[]> findMonthlyCalendarStats(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 날짜의 특정 학생의 세션 목록 조회 (시나리오, 카테고리 정보 포함)
     */
    @Query("SELECT ss FROM ScenarioSession ss " +
            "JOIN FETCH ss.scenario s " +
            "JOIN FETCH s.scenarioCategory sc " +
            "WHERE ss.student.id = :studentId " +
            "AND DATE(ss.createdAt) = :date " +
            "AND ss.isDeleted = false " +
            "ORDER BY ss.createdAt DESC")
    List<ScenarioSession> findDailySessionsByStudent(
            @Param("studentId") Long studentId,
            @Param("date") java.time.LocalDate date
    );

    /**
     * 특정 학생의 세션이 있는 날짜 목록 조회 (캐시 무효화용)
     */
    @Query("SELECT DISTINCT DATE(ss.createdAt) " +
            "FROM ScenarioSession ss " +
            "WHERE ss.student.id = :studentId " +
            "AND ss.isDeleted = false " +
            "ORDER BY DATE(ss.createdAt)")
    List<java.time.LocalDate> findSessionDatesByStudent(@Param("studentId") Long studentId);
}