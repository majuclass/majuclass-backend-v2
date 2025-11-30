package com.ssafy.a202.domain.session.dto.response;

import com.ssafy.a202.domain.session.entity.Answer;

public record AnswerResponse(
        Long answerId
) {
    public static AnswerResponse of(Answer answer) {
        return new AnswerResponse(
                answer.getId()
        );
    }
}
