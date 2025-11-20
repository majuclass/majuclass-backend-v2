package com.ssafy.a202.domain.student.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.domain.scenariosession.entity.SessionAnswer;
import com.ssafy.a202.domain.scenariosession.entity.SessionSttAnswer;
import com.ssafy.a202.domain.scenariosession.repository.ScenarioSessionRepository;
import com.ssafy.a202.domain.scenariosession.repository.SessionAnswerRepository;
import com.ssafy.a202.domain.scenariosession.repository.SessionSttAnswerRepository;
import com.ssafy.a202.domain.student.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.student.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.student.dto.response.AnswerAttemptDto;
import com.ssafy.a202.domain.student.dto.response.CalendarDayStatsDto;
import com.ssafy.a202.domain.student.dto.response.CalendarMonthlyResponse;
import com.ssafy.a202.domain.student.dto.response.CategoryStatsDto;
import com.ssafy.a202.domain.student.dto.response.DailySessionListResponse;
import com.ssafy.a202.domain.student.dto.response.SequenceStatsDto;
import com.ssafy.a202.domain.student.dto.response.SessionListItemDto;
import com.ssafy.a202.domain.student.dto.response.SessionSequenceStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentDashboardStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentResponse;
import com.ssafy.a202.domain.student.dto.response.StudentSessionsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentSessionCountDto;
import com.ssafy.a202.domain.student.entity.Student;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.student.repository.StudentRepository;
import com.ssafy.a202.domain.user.repository.UserRepository;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.constants.RedisKey;
import com.ssafy.a202.global.constants.Role;
import com.ssafy.a202.global.constants.SessionStatus;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.s3.S3UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 학생 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ScenarioSessionRepository scenarioSessionRepository;
    private final SessionAnswerRepository sessionAnswerRepository;
    private final SessionSttAnswerRepository sessionSttAnswerRepository;
    private final S3UrlService s3UrlService;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 담당 학생 목록 조회
     * - ADMIN: 자신의 학교의 모든 학생 조회
     * - USER: 자신이 담당하는 학생만 조회
     */
    @Override
    public List<StudentResponse> getStudents(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 목록 조회 (권한에 따라 다르게)
        List<Student> students;
        if (isAdmin(user)) {
            // 관리자: 학교의 모든 학생 조회
            students = studentRepository.findBySchoolAndIsDeletedFalse(user.getSchool());
            log.debug("Student list retrieved by ADMIN: userId={}, schoolId={}, count={}",
                    userId, user.getSchool().getId(), students.size());
        } else {
            // 일반 선생님: 담당 학생만 조회
            students = studentRepository.findByUserAndIsDeletedFalse(user);
            log.debug("Student list retrieved by USER: userId={}, count={}", userId, students.size());
        }

        // 3. 응답 생성
        return students.stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 학생 상세 조회
     * - ADMIN: 자신의 학교 학생이면 조회 가능
     * - USER: 자신이 담당하는 학생만 조회 가능
     */
    @Override
    public StudentResponse getStudent(Long userId, Long studentId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        log.debug("Student detail retrieved: userId={}, studentId={}",
                userId, studentId);

        // 4. 응답 생성
        return StudentResponse.from(student);
    }

    /**
     * 학생 추가
     */
    @Override
    @Transactional
    public StudentResponse createStudent(Long userId, StudentCreateRequest request) {
        // 1. 선생님 조회
        User teacher = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 생성 (학교는 선생님의 학교로 자동 설정)
        Student student = Student.builder()
                .name(request.getName())
                .school(teacher.getSchool())
                .user(teacher)
                .build();

        // 3. 저장
        Student savedStudent = studentRepository.save(student);

        log.info("Student created: userId={}, studentId={}, studentName={}",
                userId, savedStudent.getId(), savedStudent.getName());

        // 4. 응답 생성
        return StudentResponse.from(savedStudent);
    }

    /**
     * 학생 정보 수정
     * - ADMIN: 자신의 학교 학생이면 수정 가능
     * - USER: 자신이 담당하는 학생만 수정 가능
     */
    @Override
    @Transactional
    public StudentResponse updateStudent(Long userId, Long studentId, StudentUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 이름 수정
        if (request.getName() != null && !request.getName().isBlank()) {
            student.updateName(request.getName());
        }

        // 5. 담당 선생님 변경
        if (request.getUserId() != null) {
            User newTeacher = userRepository.findByIdAndIsDeletedFalse(request.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 새 선생님이 같은 학교인지 확인
            if (!student.getSchool().getId().equals(newTeacher.getSchool().getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            // 기존 선생님 ID 저장
            Long oldTeacherId = student.getUser().getId();

            // 선생님 변경
            student.changeTeacher(newTeacher);

            // 해당 학생의 세션이 있는 날짜 목록 조회
            List<LocalDateTime> sessionDateTimes = scenarioSessionRepository.findSessionDatesByStudent(studentId);
            List<LocalDate> sessionDates = sessionDateTimes.stream()
                    .map(LocalDateTime::toLocalDate)
                    .distinct()
                    .collect(Collectors.toList());

            // 기존 선생님과 새 선생님의 캘린더 캐시 무효화
            for (LocalDate date : sessionDates) {
                invalidateCalendarCache(oldTeacherId, date);  // 기존 선생님 캐시 무효화
                invalidateCalendarCache(newTeacher.getId(), date);  // 새 선생님 캐시 무효화
            }

            log.info("Student teacher changed: studentId={}, oldTeacherId={}, newTeacherId={}, invalidatedCacheDates={}",
                    studentId, oldTeacherId, newTeacher.getId(), sessionDates.size());
        }

        log.info("Student updated: userId={}, studentId={}",
                userId, studentId);

        // 6. 응답 생성
        return StudentResponse.from(student);
    }

    /**
     * 학생 삭제
     * - ADMIN: 자신의 학교 학생이면 삭제 가능
     * - USER: 자신이 담당하는 학생만 삭제 가능
     */
    @Override
    @Transactional
    public void deleteStudent(Long userId, Long studentId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 해당 학생의 세션이 있는 날짜 목록 조회
        List<LocalDateTime> sessionDateTimes = scenarioSessionRepository.findSessionDatesByStudent(studentId);
        List<LocalDate> sessionDates = sessionDateTimes.stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .collect(Collectors.toList());

        // 5. 삭제 처리 (soft delete)
        student.delete();

        // 6. 해당 학생의 세션이 있던 날짜의 캘린더 캐시 무효화
        Long teacherId = student.getUser().getId();
        for (LocalDate date : sessionDates) {
            invalidateCalendarCache(teacherId, date);
        }

        log.info("Student deleted: userId={}, studentId={}, invalidatedCacheDates={}",
                userId, studentId, sessionDates.size());
    }

    /**
     * 학생 대시보드 통계 조회
     * - 카테고리별 세션 비율 조회 (도넛 그래프용)
     */
    @Override
    public StudentDashboardStatsResponse getStudentDashboardStats(Long userId, Long studentId, int year, int month) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 년월 날짜 범위 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 5. 카테고리별 통계 조회
        List<CategoryStatsDto> categoryStats = buildCategoryStats(studentId, startDate, endDate);

        // 6. 전체 세션 개수
        int totalSessions = scenarioSessionRepository.findMonthlySessionsByStudent(
                studentId, startDate, endDate, null, null
        ).size();

        log.debug("Student dashboard stats retrieved: userId={}, studentId={}, year={}, month={}, totalSessions={}",
                userId, studentId, year, month, totalSessions);

        // 7. 응답 생성
        return StudentDashboardStatsResponse.builder()
                .categoryStats(categoryStats)
                .totalSessions(totalSessions)
                .build();
    }

    /**
     * 학생 세션 목록 조회
     * - 카테고리 및 상태 필터링 가능
     */
    @Override
    public StudentSessionsResponse getStudentSessions(Long userId, Long studentId, int year, int month, Long categoryId, SessionStatus status) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 년월 날짜 범위 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 5. 세션 목록 조회 (필터링 적용)
        List<ScenarioSession> sessions = scenarioSessionRepository.findMonthlySessionsByStudent(
                studentId, startDate, endDate, categoryId, status
        );

        // 6. 세션 목록을 DTO로 변환
        List<SessionListItemDto> sessionListItems = sessions.stream()
                .map(this::convertToSessionListItemDto)
                .collect(Collectors.toList());

        log.debug("Student sessions retrieved: userId={}, studentId={}, year={}, month={}, categoryId={}, status={}, count={}",
                userId, studentId, year, month, categoryId, status, sessionListItems.size());

        // 7. 응답 생성
        return StudentSessionsResponse.builder()
                .sessions(sessionListItems)
                .totalCount(sessionListItems.size())
                .build();
    }

    /**
     * 세션의 시퀀스별 통계 조회
     * - 각 시퀀스별로 성공 시도 번호 및 정답률 계산
     */
    @Override
    public SessionSequenceStatsResponse getSessionSequenceStats(Long userId, Long sessionId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 세션 조회
        ScenarioSession session = scenarioSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 3. 권한 체크 (세션의 학생에 대한 접근 권한)
        validateStudentAccess(user, session.getStudent());

        // 4. 세션의 모든 답변 조회 (텍스트 답변 + STT 답변)
        List<SessionAnswer> textAnswers = sessionAnswerRepository.findAllBySessionIdWithSequence(sessionId);
        List<SessionSttAnswer> sttAnswers = sessionSttAnswerRepository.findAllBySessionIdWithSequence(sessionId);

        log.debug("Text answers count: {}, STT answers count: {}", textAnswers.size(), sttAnswers.size());
        sttAnswers.forEach(answer -> {
            log.debug("STT Answer - seqId: {}, attemptNo: {}, isCorrect: {}",
                    answer.getScenarioSequence().getId(), answer.getAttemptNo(), answer.isCorrect());
        });

        // 5. 시퀀스별로 답변 정보를 통합
        // 각 시퀀스에 대해 모든 시도를 attemptNo 순으로 정렬하고 정답 여부를 추적
        Map<Long, List<AnswerAttemptDto>> answersBySequence = new HashMap<>();

        // 텍스트 답변 추가
        textAnswers.forEach(answer -> {
            Long seqId = answer.getScenarioSequence().getId();
            answersBySequence.computeIfAbsent(seqId, k -> new ArrayList<>())
                    .add(AnswerAttemptDto.builder()
                            .attemptNo(answer.getAttemptNo())
                            .isCorrect(answer.getIsCorrect())
                            .build());
        });

        // STT 답변 추가
        sttAnswers.forEach(answer -> {
            Long seqId = answer.getScenarioSequence().getId();
            answersBySequence.computeIfAbsent(seqId, k -> new ArrayList<>())
                    .add(AnswerAttemptDto.builder()
                            .attemptNo(answer.getAttemptNo())
                            .isCorrect(answer.isCorrect())
                            .build());
        });

        // 각 시퀀스의 답변들을 attemptNo 순으로 정렬
        answersBySequence.values().forEach(attempts ->
                attempts.sort(Comparator.comparingInt(AnswerAttemptDto::attemptNo)));

        // 6. 시퀀스별 통계 생성
        List<SequenceStatsDto> sequenceStats = session.getScenario().getScenarioSequences().stream()
                .filter(seq -> !seq.isDeleted())
                .sorted(Comparator.comparingInt(ScenarioSequence::getSeqNo))
                .map(sequence -> {
                    List<AnswerAttemptDto> sequenceAnswers = answersBySequence.getOrDefault(
                            sequence.getId(), Collections.emptyList());

                    // 정답을 맞춘 답변 찾기 (isCorrect = true)
                    Optional<AnswerAttemptDto> correctAnswer = sequenceAnswers.stream()
                            .filter(AnswerAttemptDto::isCorrect)
                            .findFirst();

                    Integer successAttempt = correctAnswer.map(AnswerAttemptDto::attemptNo).orElse(null);
                    Double accuracyRate = successAttempt != null ? (100.0 / successAttempt) : 0.0;
                    accuracyRate = Math.round(accuracyRate * 100.0) / 100.0; // 소수점 2자리

                    return SequenceStatsDto.builder()
                            .sequenceId(sequence.getId())
                            .sequenceNumber(sequence.getSeqNo())
                            .question(sequence.getQuestion())
                            .successAttempt(successAttempt)
                            .accuracyRate(accuracyRate)
                            .isCorrect(correctAnswer.isPresent())
                            .build();
                })
                .collect(Collectors.toList());

        // 7. 전체 시퀀스 개수 및 완료된 시퀀스 개수 계산
        int totalSequences = sequenceStats.size();
        int completedSequences = (int) sequenceStats.stream()
                .filter(SequenceStatsDto::isCorrect)
                .count();

        // 8. 평균 정답률 계산 (완료된 시퀀스들만)
        double averageAccuracy = sequenceStats.stream()
                .filter(SequenceStatsDto::isCorrect)
                .mapToDouble(SequenceStatsDto::accuracyRate)
                .average()
                .orElse(0.0);
        averageAccuracy = Math.round(averageAccuracy * 100.0) / 100.0; // 소수점 2자리

        log.debug("Session sequence stats retrieved: userId={}, sessionId={}, totalSequences={}, completedSequences={}",
                userId, sessionId, totalSequences, completedSequences);

        // 9. 응답 생성
        return SessionSequenceStatsResponse.builder()
                .sessionId(sessionId)
                .scenarioTitle(session.getScenario().getTitle())
                .sequenceStats(sequenceStats)
                .totalSequences(totalSequences)
                .completedSequences(completedSequences)
                .averageAccuracy(averageAccuracy)
                .build();
    }

    /**
     * 월별 달력 데이터 조회 (Redis 일별 캐시 적용)
     * - 담당 학생들의 일별 세션 수 통계
     */
    @Override
    public CalendarMonthlyResponse getMonthlyCalendar(Long userId, int year, int month) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 해당 월의 전체 일자 목록 생성
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // 3. 일별 캐시 조회 및 미스 날짜 수집
        List<CalendarDayStatsDto> allDailyStats = new ArrayList<>();
        List<LocalDate> cacheMissDates = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            String cacheKey = RedisKey.getCalendarDailyKey(userId, date);

            // 캐시 조회
            try {
                Object cachedObject = objectRedisTemplate.opsForValue().get(cacheKey);
                if (cachedObject != null) {
                    CalendarDayStatsDto cached = convertToCalendarDayStatsDto(cachedObject);
                    allDailyStats.add(cached);
                    log.debug("Calendar daily data retrieved from cache: userId={}, date={}", userId, date);
                } else {
                    cacheMissDates.add(date);
                }
            } catch (Exception e) {
                // 캐시 역직렬화 실패 시 캐시 미스로 처리
                log.warn("Failed to deserialize cached data, treating as cache miss: userId={}, date={}, error={}",
                        userId, date, e.getMessage());
                cacheMissDates.add(date);
            }
        }

        // 4. 캐시 미스 날짜들만 DB에서 조회
        if (!cacheMissDates.isEmpty()) {
            Map<LocalDate, CalendarDayStatsDto> dbStats = fetchDailyStatsFromDB(userId, cacheMissDates);

            // 5. 조회한 데이터를 일별로 캐싱하고 결과 리스트에 추가
            dbStats.forEach((date, stats) -> {
                String cacheKey = RedisKey.getCalendarDailyKey(userId, date);
                objectRedisTemplate.opsForValue().set(cacheKey, stats, RedisKey.CALENDAR_CACHE_TTL_DAYS, TimeUnit.DAYS);
                allDailyStats.add(stats);
                log.debug("Calendar daily data cached: userId={}, date={}", userId, date);
            });
        }

        // 6. 날짜순 정렬
        allDailyStats.sort(Comparator.comparing(CalendarDayStatsDto::date));

        log.debug("Calendar monthly data retrieved: userId={}, year={}, month={}, cacheMiss={}/{}",
                userId, year, month, cacheMissDates.size(), daysInMonth);

        // 7. 응답 생성
        return CalendarMonthlyResponse.builder()
                .year(year)
                .month(month)
                .dailyStats(allDailyStats)
                .totalDays(daysInMonth)
                .build();
    }

    /**
     * 특정 날짜의 특정 학생 세션 목록 조회
     */
    @Override
    public DailySessionListResponse getDailySessions(Long userId, Long studentId, LocalDate date) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 3. 권한 체크
        validateStudentAccess(user, student);

        // 4. 특정 날짜의 세션 목록 조회
        List<ScenarioSession> sessions = scenarioSessionRepository.findDailySessionsByStudent(studentId, date);

        // 5. 세션 목록을 DTO로 변환
        List<SessionListItemDto> sessionListItems = sessions.stream()
                .map(this::convertToSessionListItemDto)
                .collect(Collectors.toList());

        log.debug("Daily sessions retrieved: userId={}, studentId={}, date={}, count={}",
                userId, studentId, date, sessionListItems.size());

        // 6. 응답 생성
        return DailySessionListResponse.builder()
                .date(date)
                .studentId(studentId)
                .studentName(student.getName())
                .sessions(sessionListItems)
                .totalCount(sessionListItems.size())
                .build();
    }

    /**
     * 달력 캐시 무효화
     * - 새 세션 생성/삭제 시 해당 날짜의 캐시만 삭제
     */
    @Override
    public void invalidateCalendarCache(Long userId, LocalDate date) {
        String cacheKey = RedisKey.getCalendarDailyKey(userId, date);
        objectRedisTemplate.delete(cacheKey);
        log.debug("Calendar daily cache invalidated: userId={}, date={}", userId, date);
    }

    // ================================
    // Private Helper Methods
    // ================================

    /**
     * 관리자 권한 확인
     */
    private boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    /**
     * 학생 접근 권한 검증
     * - ADMIN: 같은 학교 학생이면 접근 가능
     * - USER: 담당 학생만 접근 가능
     */
    private void validateStudentAccess(User user, Student student) {
        if (isAdmin(user)) {
            // 관리자: 같은 학교 학생인지 확인
            if (!student.getSchool().getId().equals(user.getSchool().getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            // 일반 선생님: 담당 학생인지 확인
            if (!student.getUser().getId().equals(user.getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }
    }

    /**
     * 카테고리별 통계 생성
     */
    private List<CategoryStatsDto> buildCategoryStats(Long studentId, LocalDateTime startDate, LocalDateTime endDate) {
        // 카테고리별 세션 개수 조회
        List<Object[]> rawStats = scenarioSessionRepository.findCategoryStatsByStudent(studentId, startDate, endDate);

        // 전체 세션 개수 계산
        long totalCount = rawStats.stream()
                .mapToLong(row -> (Long) row[2])
                .sum();

        // CategoryStatsDto 리스트 생성
        return rawStats.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    String categoryName = (String) row[1];
                    Long count = (Long) row[2];
                    double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;

                    return CategoryStatsDto.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
                            .sessionCount(count.intValue())
                            .percentage(Math.round(percentage * 100.0) / 100.0) // 소수점 2자리
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * ScenarioSession을 SessionListItemDto로 변환
     */
    private SessionListItemDto convertToSessionListItemDto(ScenarioSession session) {
        String thumbnailUrl = s3UrlService.generateUrl(session.getScenario().getThumbnailS3Key());

        return SessionListItemDto.builder()
                .sessionId(session.getId())
                .scenarioId(session.getScenario().getId())
                .scenarioTitle(session.getScenario().getTitle())
                .thumbnailUrl(thumbnailUrl)
                .categoryId(session.getScenario().getScenarioCategory().getId())
                .categoryName(session.getScenario().getScenarioCategory().getCategoryName())
                .status(session.getSessionStatus())
                .createdAt(session.getCreatedAt())
                .build();
    }

    /**
     * DB에서 특정 날짜들의 달력 데이터 조회
     * @param userId 사용자 ID
     * @param dates 조회할 날짜 리스트
     * @return 날짜별 통계 데이터 맵
     */
    private Map<LocalDate, CalendarDayStatsDto> fetchDailyStatsFromDB(Long userId, List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 조회 기간 계산 (최소 날짜 ~ 최대 날짜)
        LocalDate minDate = dates.stream().min(LocalDate::compareTo).orElseThrow();
        LocalDate maxDate = dates.stream().max(LocalDate::compareTo).orElseThrow();
        LocalDateTime startDate = minDate.atStartOfDay();
        LocalDateTime endDate = maxDate.atTime(23, 59, 59);

        // 2. DB에서 일별 학생별 세션 수 집계 조회
        List<Object[]> rawStats = scenarioSessionRepository.findMonthlyCalendarStats(userId, startDate, endDate);

        // 3. 날짜별로 그룹화
        Map<LocalDate, List<Object[]>> statsByDate = rawStats.stream()
                .collect(Collectors.groupingBy(row -> ((Date) row[0]).toLocalDate()));

        // 4. 요청한 날짜들에 대해서만 CalendarDayStatsDto 생성
        Map<LocalDate, CalendarDayStatsDto> result = new HashMap<>();
        for (LocalDate date : dates) {
            List<Object[]> dayStats = statsByDate.get(date);

            if (dayStats != null && !dayStats.isEmpty()) {
                // 학생별 세션 수 DTO 생성
                List<StudentSessionCountDto> studentSessions = dayStats.stream()
                        .map(row -> StudentSessionCountDto.builder()
                                .studentId((Long) row[1])
                                .studentName((String) row[2])
                                .sessionCount(((Long) row[3]).intValue())
                                .build())
                        .collect(Collectors.toList());

                // 해당 날짜의 전체 세션 수 계산
                int totalSessionCount = studentSessions.stream()
                        .mapToInt(StudentSessionCountDto::sessionCount)
                        .sum();

                CalendarDayStatsDto stats = CalendarDayStatsDto.builder()
                        .date(date)
                        .studentSessions(studentSessions)
                        .totalSessionCount(totalSessionCount)
                        .build();

                result.put(date, stats);
            } else {
                // 해당 날짜에 세션이 없는 경우 빈 데이터 생성
                CalendarDayStatsDto emptyStats = CalendarDayStatsDto.builder()
                        .date(date)
                        .studentSessions(Collections.emptyList())
                        .totalSessionCount(0)
                        .build();

                result.put(date, emptyStats);
            }
        }

        return result;
    }

    /**
     * Redis에서 가져온 객체를 CalendarDayStatsDto로 안전하게 변환
     * - Spring Boot Loader 환경에서 LinkedHashMap으로 역직렬화되는 문제 해결
     */
    private CalendarDayStatsDto convertToCalendarDayStatsDto(Object cachedObject) {
        if (cachedObject instanceof CalendarDayStatsDto) {
            return (CalendarDayStatsDto) cachedObject;
        }

        // LinkedHashMap으로 역직렬화된 경우 JSON을 거쳐 변환 (중첩 타입 안전 처리)
        try {
            String json = objectMapper.writeValueAsString(cachedObject);
            return objectMapper.readValue(json, CalendarDayStatsDto.class);
        } catch (Exception e) {
            log.error("Failed to convert cached object to CalendarDayStatsDto", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}