package com.ssafy.a202.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 공통
    SUCCESS_DEFAULT("요청이 성공적으로 처리되었습니다."),
    LOGIN_SUCCESS("로그인이 성공적으로 처리되었습니다."),
    SIGNUP_SUCCESS("회원가입이 성공적으로 완료되었습니다."),

    // 시나리오,
    SCENARIO_CREATE_SUCCESS("시나리오가 성공적으로 생성되었습니다."),
    SCENARIO_GET_LIST_SUCCESS("시나리오 목록을 성공적으로 조회했습니다."),
    SCENARIO_GET_DETAIL_SUCCESS("시나리오 상세를 성공적으로 조회했습니다."),
    SCENARIO_UPDATE_SUCCESS("시나리오가 성공적으로 수정되었습니다."),
    SCENARIO_DELETE_SUCCESS("시나리오가 성공적으로 삭제되었습니다."),

    // 시나리오 난이도,
    DIFFICULTY_LEVEL_GET_SUCCESS("시나리오 난이도 목록을 성공적으로 조회했습니다."),

    // S3
    PRESIGNED_URL_GET_PUT_SUCCESS("PUT용 Presigned URL을 성공적으로 발급했습니다."),

    // 학생
    STUDENT_CREATED_SUCCESS("학생이 성공적으로 생성되었습니다."),
    STUDENT_GET_LIST_SUCCESS("학생 목록을 성공적으로 조회했습니다."),
    STUDENT_UPDATE_SUCCESS("학생이 성공적으로 수정되었습니다."),
    STUDENT_DELETE_SUCCESS("학생이 성공적으로 삭제되었습니다."),

    // 세션
    SESSION_CREATE_SUCCESS("세션이 성공적으로 시작되었습니다."),
    SESSION_FINISH_SUCCESS("세션이 성공적으로 완료되었습니다."),
    SESSION_ABORT_SUCCESS("세션이 성공적으로 중단되었습니다."),

    // 세션 정답
    ANSWER_CREATE_SUCCESS("정답이 성공적으로 저장되었습니다.");

    private final String message;
}
