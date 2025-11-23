package com.ssafy.a202.domain.user.entity;

public enum UserRole {
    TEACHER("선생님"),
    ADMIN("운영자"),
    ORG_ADMIN("기관책임자");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
