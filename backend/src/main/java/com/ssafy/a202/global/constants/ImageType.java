package com.ssafy.a202.global.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시나리오 이미지 타입
 */
@Getter
@RequiredArgsConstructor
public enum ImageType {
    THUMBNAIL("썸네일"),
    BACKGROUND("배경"),
    OPTION("옵션");

    private final String description;
}