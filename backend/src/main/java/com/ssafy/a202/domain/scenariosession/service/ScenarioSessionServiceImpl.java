package com.ssafy.a202.domain.scenariosession.service;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenario.entity.SeqOption;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.domain.scenariosession.dto.request.*;
import com.ssafy.a202.domain.scenariosession.dto.response.*;
import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.domain.scenariosession.entity.SessionAnswer;
import com.ssafy.a202.domain.scenariosession.repository.ScenarioSessionRepository;
import com.ssafy.a202.domain.scenariosession.repository.SessionAnswerRepository;
import com.ssafy.a202.domain.user.entity.Student;
import com.ssafy.a202.domain.user.repository.StudentRepository;
import com.ssafy.a202.global.constants.SessionStatus;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.s3.S3UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 시나리오 세션 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioSessionServiceImpl implements ScenarioSessionService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioSessionRepository scenarioSessionRepository;
    private final SessionAnswerRepository sessionAnswerRepository;
    private final StudentRepository studentRepository;
    private final S3UrlService s3UrlService;

    @Override
    @Transactional
    public SessionStartResponse startSession(SessionStartRequest request) {
        log.info("Starting session for student ID: {}, scenario ID: {}",
                request.getStudentId(), request.getScenarioId());

        // 학생 조회
        Student student = studentRepository.findByIdAndIsDeletedFalse(request.getStudentId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(request.getScenarioId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 접근 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        // 세션 생성
        ScenarioSession session = ScenarioSession.builder()
                .student(student)
                .scenario(scenario)
                .sessionStatus(SessionStatus.IN_PROGRESS)
                .isDeleted(false)
                .build();

        ScenarioSession savedSession = scenarioSessionRepository.save(session);

        log.info("Session created with ID: {} for student ID: {}, scenario ID: {}",
                savedSession.getId(), request.getStudentId(), request.getScenarioId());

        return SessionStartResponse.from(savedSession);
    }

    @Override
    @Transactional
    public AudioUploadUrlResponse generateAudioUploadUrl(AudioUploadUrlRequest request) {
        log.info("Generating audio upload URL for session ID: {}, sequence number: {}",
                request.getSessionId(), request.getSequenceNumber());

        // 세션 조회 및 검증
        ScenarioSession session = scenarioSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.isDeleted()) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 시퀀스 조회 및 검증
        ScenarioSequence sequence = session.getScenario().getScenarioSequences().stream()
                .filter(seq -> seq.getSeqNo() == request.getSequenceNumber() && !seq.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND));

        // 시도 횟수 계산
        long attemptCount = sessionAnswerRepository.countByScenarioSessionAndScenarioSequence(session, sequence);
        int attemptNo = (int) attemptCount + 1;

        // S3 키 자동 생성: students/{student_id}/sessions/{session_id}/seq_{seq_no}_attempt_{attempt_no}.wav
        Long studentId = session.getStudent().getId();
        String fileExtension = getFileExtension(request.getContentType());
        String s3Key = String.format("students/%d/sessions/%d/seq_%d_attempt_%d%s",
                studentId,
                request.getSessionId(),
                request.getSequenceNumber(),
                attemptNo,
                fileExtension
        );

        log.info("Generated S3 key: {}", s3Key);

        // Lambda API 호출하여 Presigned URL 생성
        Map<String, String> lambdaResponse = s3UrlService.generateUploadPresignedUrl(
                s3Key,
                "putObject",
                request.getContentType()
        );

        String uploadUrl = lambdaResponse.get("url");
        String fileName = lambdaResponse.get("fileName");

        log.info("Generated audio upload URL for session ID: {}, S3 key: {}",
                request.getSessionId(), fileName);

        return AudioUploadUrlResponse.builder()
                .url(uploadUrl)
                .fileName(fileName)
                .build();
    }

    /**
     * Content-Type으로부터 파일 확장자 추출
     */
    private String getFileExtension(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "audio/wav", "audio/wave", "audio/x-wav" -> ".wav";
            case "audio/mpeg", "audio/mp3" -> ".mp3";
            case "audio/mp4" -> ".m4a";
            case "audio/webm" -> ".webm";
            case "audio/ogg" -> ".ogg";
            default -> ".wav";  // 기본값
        };
    }

    @Override
    @Transactional
    public AnswerCheckResponse submitAnswer(AnswerSubmitRequest request) {
        log.info("Checking answer for session ID: {}, scenario ID: {}, sequence number: {}",
                request.getSessionId(), request.getScenarioId(), request.getSequenceNumber());

        // 세션 조회
        ScenarioSession session = scenarioSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 삭제된 세션은 접근 불가
        if (session.isDeleted()) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(request.getScenarioId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 접근 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        // 해당 시퀀스 번호의 시퀀스 조회
        ScenarioSequence sequence = scenario.getScenarioSequences().stream()
                .filter(seq -> seq.getSeqNo() == request.getSequenceNumber() && !seq.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND));

        // 사용자가 선택한 옵션 찾기
        SeqOption selectedOption = sequence.getOptions().stream()
                .filter(option -> option.getId().equals(request.getSelectedOptionId()) && !option.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_OPTION_SELECTED));

        // 정답 여부 확인
        boolean isCorrect = selectedOption.isAnswer();

        // 시도 횟수 계산
        long attemptCount = sessionAnswerRepository.countByScenarioSessionAndScenarioSequence(session, sequence);
        int attemptNo = (int) attemptCount + 1;

        // 모든 시도(정답/오답) 저장
        SessionAnswer sessionAnswer = SessionAnswer.builder()
                .scenarioSession(session)
                .scenarioSequence(sequence)
                .answerS3Key(null)  // 하/중 난이도는 음성 없음
                .attemptNo(attemptNo)
                .isCorrect(isCorrect)
                .build();

        sessionAnswerRepository.save(sessionAnswer);

        log.info("Saved answer - sessionId: {}, sequenceNumber: {}, attemptNo: {}, isCorrect: {}",
                session.getId(), sequence.getSeqNo(), attemptNo, isCorrect);

        return AnswerCheckResponse.builder()
                .scenarioId(scenario.getId())
                .sequenceId(sequence.getId())
                .sequenceNumber(sequence.getSeqNo())
                .submittedOptionId(selectedOption.getId())
                .isCorrect(isCorrect)
                .attemptNo(attemptNo)
                .build();
    }

    @Override
    @Transactional
    public AudioSubmitResponse submitAudioAnswer(AudioSubmitRequest request) {
        log.info("Submitting audio answer for session ID: {}, scenario ID: {}, sequence number: {}, S3 key: {}",
                request.getSessionId(), request.getScenarioId(), request.getSequenceNumber(), request.getAudioS3Key());

        // 세션 조회
        ScenarioSession session = scenarioSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 삭제된 세션은 접근 불가
        if (session.isDeleted()) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(request.getScenarioId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 접근 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        // 해당 시퀀스 번호의 시퀀스 조회
        ScenarioSequence sequence = scenario.getScenarioSequences().stream()
                .filter(seq -> seq.getSeqNo() == request.getSequenceNumber() && !seq.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND));

        // TODO: FastAPI STT 분석 API 호출
        // 1. request.getAudioS3Key()를 FastAPI에 전달
        // 2. FastAPI에서 STT 분석 후 정답 여부 반환
        // 3. 분석 실패 시 적절한 예외 처리
        boolean isCorrect = false;  // 임시값 (FastAPI 연동 후 실제 분석 결과로 대체)

        // 시도 횟수 계산
        long attemptCount = sessionAnswerRepository.countByScenarioSessionAndScenarioSequence(session, sequence);
        int attemptNo = (int) attemptCount + 1;

        // SessionAnswer 저장 (음성 파일 + STT 분석 결과)
        SessionAnswer sessionAnswer = SessionAnswer.builder()
                .scenarioSession(session)
                .scenarioSequence(sequence)
                .answerS3Key(request.getAudioS3Key())
                .attemptNo(attemptNo)
                .isCorrect(isCorrect)  // FastAPI 분석 결과
                .build();

        sessionAnswerRepository.save(sessionAnswer);

        log.info("Saved audio answer - sessionId: {}, sequenceNumber: {}, attemptNo: {}, isCorrect: {}",
                session.getId(), sequence.getSeqNo(), attemptNo, isCorrect);

        return AudioSubmitResponse.builder()
                .scenarioId(scenario.getId())
                .sequenceId(sequence.getId())
                .sequenceNumber(sequence.getSeqNo())
                .audioS3Key(request.getAudioS3Key())
                .isCorrect(isCorrect)
                .attemptNo(attemptNo)
                .build();
    }

    @Override
    @Transactional
    public SessionCompleteResponse completeSession(SessionCompleteRequest request) {
        log.info("Completing session ID: {}", request.getSessionId());

        // 세션 조회 및 검증
        ScenarioSession session = scenarioSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.isDeleted()) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 이미 완료된 세션이면 에러 반환
        if (session.getSessionStatus() == SessionStatus.COMPLETED) {
            log.warn("Session ID: {} is already completed", request.getSessionId());
            throw new CustomException(ErrorCode.SESSION_ALREADY_COMPLETED);
        }

        // 세션 완료 처리
        session.complete();
        scenarioSessionRepository.save(session);

        log.info("Session ID: {} completed successfully", request.getSessionId());

        return SessionCompleteResponse.from(session);
    }
}