package com.ssafy.a202.domain.user.service;

import com.ssafy.a202.domain.user.dto.request.UserUpdateRequest;
import com.ssafy.a202.domain.user.dto.response.UserResponse;
import com.ssafy.a202.domain.user.dto.response.UserUpdateResponse;

import java.util.List;

/**
 * 사용자 서비스 인터페이스
 */
public interface UserService {

    /**
     * 같은 학교 사용자 목록 조회
     * @param userId 조회 요청한 사용자 ID
     * @return 같은 학교 사용자 목록
     */
    List<UserResponse> getUsers(Long userId);

    /**
     * 사용자 상세 조회
     * @param userId 조회 요청한 사용자 ID
     * @param targetUserId 조회할 대상 사용자 ID
     * @return 사용자 상세 정보
     */
    UserResponse getUser(Long userId, Long targetUserId);

    /**
     * 회원 정보 수정 (본인만 가능)
     * @param userId 수정할 사용자 ID
     * @param request 수정할 정보
     * @return 수정된 사용자 정보
     */
    UserUpdateResponse updateUser(Long userId, UserUpdateRequest request);

    /**
     * 회원 탈퇴 (본인만 가능, soft delete)
     * @param userId 탈퇴할 사용자 ID
     * @param accessToken 현재 사용 중인 액세스 토큰
     * @param refreshToken 현재 사용 중인 리프레시 토큰
     */
    void withdrawUser(Long userId, String accessToken, String refreshToken);

    /**
     * 관리자가 사용자 삭제 (soft delete)
     * @param userId 삭제할 사용자 ID
     */
    void deleteUserByAdmin(Long userId);
}