package com.ssafy.a202.global.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시나리오 난이도
 */
@Getter
@RequiredArgsConstructor
public enum Difficulty {
    EASY("하"),
    NORMAL("중"),
    HARD("상");

    private final String description;
}
