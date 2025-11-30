package com.ssafy.a202.domain.session.dto.response;

import com.ssafy.a202.domain.session.entity.Session;

public record SessionResponse(
        Long sessionId
) {
    public static SessionResponse of(Session session) {
        return new SessionResponse(
                session.getId()
        );
    }
}
