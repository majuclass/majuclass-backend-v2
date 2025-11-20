package com.ssafy.a202.global.scheduler;

import com.ssafy.a202.global.constants.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Set;

/**
 * 달력 캐시 정리 스케줄러
 * 매월 1일 00시에 3개월 이전 캐시를 삭제하여 메모리 절약
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarCacheScheduler {

    private final RedisTemplate<String, Object> objectRedisTemplate;

    /**
     * 3개월 이전 달력 캐시 삭제
     * 매월 1일 00:00 실행
     * 예: 11월 1일 실행 시 → 8월 데이터 삭제 (9, 10, 11월만 유지)
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void cleanupOldCalendarCache() {
        try {
            // 3개월 이전 년월 계산
            YearMonth threeMonthsAgo = YearMonth.now().minusMonths(3);
            int year = threeMonthsAgo.getYear();
            int month = threeMonthsAgo.getMonthValue();

            // 해당 월의 모든 캐시 키 패턴 생성
            String pattern = RedisKey.getCalendarMonthlyPattern(year, month);

            log.info("Starting calendar cache cleanup for {}-{}", year, month);

            // 패턴에 맞는 모든 키 조회
            Set<String> keys = objectRedisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                // 키 삭제
                Long deletedCount = objectRedisTemplate.delete(keys);
                log.info("Calendar cache cleanup completed: deleted {} keys for {}-{}",
                        deletedCount, year, month);
            } else {
                log.info("No calendar cache found for {}-{}", year, month);
            }

        } catch (Exception e) {
            log.error("Failed to cleanup old calendar cache", e);
        }
    }
}