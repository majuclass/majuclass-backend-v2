package com.ssafy.a202.domain.session.entity;

public enum SessionStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    ABORTED("중단");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
