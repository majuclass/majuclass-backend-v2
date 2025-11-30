package com.ssafy.a202.domain.session.service;

import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.domain.session.dto.request.AnswerCreateRequest;
import com.ssafy.a202.domain.session.dto.response.AnswerResponse;
import com.ssafy.a202.domain.session.entity.Answer;
import com.ssafy.a202.domain.session.entity.Session;
import com.ssafy.a202.domain.session.entity.SessionStatus;
import com.ssafy.a202.domain.session.repository.AnswerRepository;
import com.ssafy.a202.domain.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final SessionRepository sessionRepository;

    // todo : isCorrect를 클라이언트가 보내기 때문에 정답 여부를 조작할 수 있음.(기술 부채. 파이썬 서버를 내부 서버끼리 소통하도록 합쳐야함)
    @Transactional
    public AnswerResponse create(Long sessionId, AnswerCreateRequest request) {
        Session session = sessionRepository.findByIdAndDeletedAtIsNull(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new CustomException(ErrorCode.SESSION_UPDATE_DENIED);
        }

        Answer answer = Answer.of(session, request);
        answerRepository.save(answer);

        return AnswerResponse.of(answer);
    }
}
