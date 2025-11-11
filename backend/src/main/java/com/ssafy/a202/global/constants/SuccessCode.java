package com.ssafy.a202.global.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 성공 응답 메시지를 정의하는 열거형
 */
@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 공통
    SUCCESS_DEFAULT("요청이 성공적으로 처리되었습니다"),

    // 인증 관련
    LOGIN_SUCCESS("로그인이 성공했습니다"),
    LOGOUT_SUCCESS("로그아웃이 완료되었습니다"),
    TOKEN_REFRESH_SUCCESS("토큰 갱신이 완료되었습니다"),
    SIGNUP_SUCCESS("회원가입이 완료되었습니다"),

    // 사용자 관련
    USER_PROFILE_SUCCESS("프로필 조회가 완료되었습니다"),
    USER_LIST_SUCCESS("사용자 목록 조회가 완료되었습니다"),
    USER_DETAIL_SUCCESS("사용자 상세 조회가 완료되었습니다"),
    USER_CREATE_SUCCESS("사용자가 성공적으로 생성되었습니다"),
    USER_UPDATE_SUCCESS("사용자 정보가 성공적으로 수정되었습니다"),
    USER_DELETE_SUCCESS("사용자가 성공적으로 삭제되었습니다"),

    // 학생 관련
    STUDENT_LIST_SUCCESS("학생 목록 조회가 완료되었습니다"),
    STUDENT_DETAIL_SUCCESS("학생 상세 조회가 완료되었습니다"),
    STUDENT_CREATE_SUCCESS("학생이 성공적으로 추가되었습니다"),
    STUDENT_UPDATE_SUCCESS("학생 정보가 성공적으로 수정되었습니다"),
    STUDENT_DELETE_SUCCESS("학생이 성공적으로 삭제되었습니다"),

    // 대시보드 관련
    CATEGORY_STATS_SUCCESS("카테고리별 통계 조회가 완료되었습니다"),
    MONTHLY_SESSION_LIST_SUCCESS("월별 세션 목록 조회가 완료되었습니다"),
    SESSION_SEQUENCE_STATS_SUCCESS("세션 시퀀스별 통계 조회가 완료되었습니다"),

    // 카테고리 관련
    CATEGORY_LIST_SUCCESS("카테고리 목록 조회가 완료되었습니다"),

    // 시나리오 관련
    SCENARIO_LIST_SUCCESS("시나리오 목록 조회가 완료되었습니다"),
    SCENARIO_DETAIL_SUCCESS("시나리오 상세 조회가 완료되었습니다"),
    SCENARIO_CREATE_SUCCESS("시나리오가 성공적으로 생성되었습니다"),
    SCENARIO_UPDATE_SUCCESS("시나리오가 성공적으로 수정되었습니다"),
    SCENARIO_DELETE_SUCCESS("시나리오가 성공적으로 삭제되었습니다"),

    // 시나리오 세션/시뮬레이션 관련
    SESSION_START_SUCCESS("세션이 성공적으로 시작되었습니다"),
    SESSION_COMPLETE_SUCCESS("세션이 성공적으로 완료되었습니다"),
    IMAGE_UPLOAD_URL_GENERATED("이미지 업로드 URL이 생성되었습니다"),
    AUDIO_UPLOAD_URL_GENERATED("음성 업로드 URL이 생성되었습니다"),
    SIMULATION_RETRIEVE_SUCCESS("시뮬레이션 조회가 완료되었습니다"),
    SEQUENCE_RETRIEVE_SUCCESS("시퀀스 조회가 완료되었습니다"),
    OPTIONS_RETRIEVE_SUCCESS("옵션 목록 조회가 완료되었습니다"),
    ANSWER_CHECK_SUCCESS("답안 검증이 완료되었습니다"),
    AUDIO_ANSWER_CHECK_SUCCESS("음성 답안 검증이 완료되었습니다");

    private final String message;
}
