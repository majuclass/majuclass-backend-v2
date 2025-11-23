package com.ssafy.a202.domain.scenario.entity;

public enum DifficultyLevel {
    HARD("상"),
    MEDIUM("중"),
    EASY("하");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
