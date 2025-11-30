package com.ssafy.a202.domain.session.service;

import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.domain.session.dto.request.SessionStartRequest;
import com.ssafy.a202.domain.session.dto.response.SessionResponse;
import com.ssafy.a202.domain.session.entity.Session;
import com.ssafy.a202.domain.session.entity.SessionStatus;
import com.ssafy.a202.domain.session.repository.SessionRepository;
import com.ssafy.a202.domain.student.entity.Student;
import com.ssafy.a202.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public SessionResponse create(SessionStartRequest request) {

        Student student = studentRepository.findByIdAndDeletedAtIsNull(request.studentId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        Session session = Session.of(student, request);
        sessionRepository.save(session);

        return SessionResponse.of(session);
    }

    @Transactional
    public void finish(Long sessionId) {
        Session session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new CustomException(ErrorCode.SESSION_FINISH_FAIL);
        }
        session.finish();
    }

    @Transactional
    public void abort(Long sessionId) {
        Session session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getSessionStatus() == SessionStatus.COMPLETED ||
                session.getSessionStatus() == SessionStatus.ABORTED) {
            throw new CustomException(ErrorCode.SESSION_ABORT_FAIL);
        }

        session.abort();
    }
}
