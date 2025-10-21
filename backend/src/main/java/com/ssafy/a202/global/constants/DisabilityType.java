package com.ssafy.a202.global.constants;

import lombok.Getter;

@Getter
public enum DisabilityType {
    INTELLECTUAL("지적장애"),
    AUTISM("자폐성장애"),
    EMOTIONAL("정서행동장애"),
    ADHD("ADHD"),
    LEARNING("학습장애"),
    PHYSICAL("지체장애"),
    OTHER("기타");

    private final String description;

    DisabilityType(String description) {
        this.description = description;
    }
}