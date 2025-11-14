package com.ssafy.a202.global.constants;

import java.time.LocalDate;

/**
 * Redis 캐시 키 상수 클래스
 * 계층적 구조로 관리: calendar:{year}:{month}:{day}:{userId}
 */
public final class RedisKey {

    private RedisKey() {
        // 인스턴스화 방지
    }

    // ================================
    // 달력 관련 캐시
    // ================================

    /**
     * 달력 캐시 루트 접두사
     */
    private static final String CALENDAR_PREFIX = "calendar";

    /**
     * 달력 캐시 TTL (일)
     * 3개월치 데이터 보관을 위해 120일(4개월)로 설정
     * 예: 9월 1일 생성 캐시가 11월 말까지 유지되도록 여유 확보
     * 스케줄러로 매월 1일에 3개월 이전 캐시를 능동적으로 삭제하며, TTL은 백업 안전장치
     */
    public static final long CALENDAR_CACHE_TTL_DAYS = 120;

    /**
     * 일별 달력 캐시 키 생성
     * 형식: calendar:{year}:{month}:{day}:{userId}
     *
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월 (1-12)
     * @param day 일 (1-31)
     * @return Redis 캐시 키
     */
    public static String getCalendarDailyKey(Long userId, int year, int month, int day) {
        return String.format("%s:%d:%d:%d:%d", CALENDAR_PREFIX, year, month, day, userId);
    }

    /**
     * 일별 달력 캐시 키 생성 (LocalDate 버전)
     * 형식: calendar:{year}:{month}:{day}:{userId}
     *
     * @param userId 사용자 ID
     * @param date 날짜
     * @return Redis 캐시 키
     */
    public static String getCalendarDailyKey(Long userId, LocalDate date) {
        return getCalendarDailyKey(userId, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * 특정 날짜의 모든 사용자 캐시 키 패턴
     * 형식: calendar:{year}:{month}:{day}:*
     * 사용 예: 특정 날짜의 모든 사용자 캐시 삭제
     *
     * @param year 년도
     * @param month 월 (1-12)
     * @param day 일 (1-31)
     * @return Redis 키 패턴
     */
    public static String getCalendarDailyPattern(int year, int month, int day) {
        return String.format("%s:%d:%d:%d:*", CALENDAR_PREFIX, year, month, day);
    }

    /**
     * 특정 월의 모든 캐시 키 패턴
     * 형식: calendar:{year}:{month}:*:*
     * 사용 예: 특정 월의 모든 캐시 삭제 (스케줄러)
     *
     * @param year 년도
     * @param month 월 (1-12)
     * @return Redis 키 패턴
     */
    public static String getCalendarMonthlyPattern(int year, int month) {
        return String.format("%s:%d:%d:*:*", CALENDAR_PREFIX, year, month);
    }

    /**
     * 특정 사용자의 모든 달력 캐시 키 패턴
     * 형식: calendar:*:*:*:{userId}
     * 사용 예: 사용자 삭제 시 모든 캐시 삭제
     *
     * @param userId 사용자 ID
     * @return Redis 키 패턴
     */
    public static String getCalendarUserPattern(Long userId) {
        return String.format("%s:*:*:*:%d", CALENDAR_PREFIX, userId);
    }
}