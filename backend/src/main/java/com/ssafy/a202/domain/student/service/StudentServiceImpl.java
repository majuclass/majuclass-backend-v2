package com.ssafy.a202.domain.student.service;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.domain.scenariosession.entity.SessionAnswer;
import com.ssafy.a202.domain.scenariosession.repository.ScenarioSessionRepository;
import com.ssafy.a202.domain.scenariosession.repository.SessionAnswerRepository;
import com.ssafy.a202.domain.student.dto.request.StudentCreateRequest;
import com.ssafy.a202.domain.student.dto.request.StudentUpdateRequest;
import com.ssafy.a202.domain.student.dto.response.CategoryStatsDto;
import com.ssafy.a202.domain.student.dto.response.SequenceStatsDto;
import com.ssafy.a202.domain.student.dto.response.SessionListItemDto;
import com.ssafy.a202.domain.student.dto.response.SessionSequenceStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentDashboardStatsResponse;
import com.ssafy.a202.domain.student.dto.response.StudentResponse;
import com.ssafy.a202.domain.student.dto.response.StudentSessionsResponse;
import com.ssafy.a202.domain.student.entity.Student;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.student.repository.StudentRepository;
import com.ssafy.a202.domain.user.repository.UserRepository;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.constants.Role;
import com.ssafy.a202.global.constants.SessionStatus;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.s3.S3UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
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
    private final S3UrlService s3UrlService;

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

            student.changeTeacher(newTeacher);
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

        // 4. 삭제 처리 (soft delete)
        student.delete();

        log.info("Student deleted: userId={}, studentId={}",
                userId, studentId);
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

        // 4. 세션의 모든 답변 조회 (시퀀스 정보 포함)
        List<SessionAnswer> answers = sessionAnswerRepository.findAllBySessionIdWithSequence(sessionId);

        // 5. 시퀀스별로 그룹화
        Map<Long, List<SessionAnswer>> answersBySequence = answers.stream()
                .collect(Collectors.groupingBy(
                        answer -> answer.getScenarioSequence().getId()
                ));

        // 6. 시퀀스별 통계 생성
        List<SequenceStatsDto> sequenceStats = session.getScenario().getScenarioSequences().stream()
                .filter(seq -> !seq.isDeleted())
                .sorted(Comparator.comparingInt(ScenarioSequence::getSeqNo))
                .map(sequence -> {
                    List<SessionAnswer> sequenceAnswers = answersBySequence.getOrDefault(sequence.getId(), Collections.emptyList());

                    // 정답을 맞춘 답변 찾기 (isCorrect = true)
                    Optional<SessionAnswer> correctAnswer = sequenceAnswers.stream()
                            .filter(SessionAnswer::getIsCorrect)
                            .findFirst();

                    Integer successAttempt = correctAnswer.map(SessionAnswer::getAttemptNo).orElse(null);
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
}