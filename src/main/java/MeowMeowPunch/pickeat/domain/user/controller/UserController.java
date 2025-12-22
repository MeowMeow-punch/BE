package MeowMeowPunch.pickeat.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import MeowMeowPunch.pickeat.domain.user.dto.request.FocusUpdateRequest;
import MeowMeowPunch.pickeat.domain.user.dto.request.UserUpdateRequest;
import MeowMeowPunch.pickeat.domain.user.dto.response.MyPageResponse;
import MeowMeowPunch.pickeat.domain.user.dto.response.UserGroupResponse;
import MeowMeowPunch.pickeat.domain.user.service.UserService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import MeowMeowPunch.pickeat.global.jwt.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;

/**
 * [User][Controller] UserController
 * 닉네임 중복 확인, 소속 검색 등 사용자 관련 API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * [API] 닉네임 중복 체크
     * <p>
     * 회원이 입력한 닉네임의 사용 가능 여부를 확인합니다.
     * </p>
     * <ul>
     * <li>성공: 200 OK (사용 가능)</li>
     * <li>실패: 409 Conflict (이미 사용 중)</li>
     * </ul>
     *
     * @param nickname 검증할 닉네임
     * @return 사용 가능 여부 메시지
     */
    @GetMapping("/nickname")
    public ResTemplate<Void> checkNickname(@RequestParam String nickname) {
        userService.checkNickname(nickname);
        return ResTemplate.success(HttpStatus.OK, "해당 닉네임은 사용가능합니다.");
    }

    /**
     * [API] 소속(그룹) 검색
     * <p>
     * 키워드로 그룹을 검색하여 목록을 반환합니다.
     * </p>
     * <ul>
     * <li>성공: 200 OK와 그룹 리스트 반환 (결과가 없으면 빈 리스트 data: [])</li>
     * </ul>
     *
     * @param keyword 검색 키워드
     * @return 검색된 그룹 리스트 (빈 리스트 포함)
     */
    @GetMapping("/groupSearch")
    public ResTemplate<List<UserGroupResponse>> searchGroups(@RequestParam String keyword) {
        List<UserGroupResponse> groups = userService.searchGroups(keyword);
        return ResTemplate.success(HttpStatus.OK, "그룹명 조회 성공", groups);
    }

    /**
     * [API] 마이페이지(내 정보) 조회
     * <p>
     * 로그인한 사용자의 프로필, 활동 요약, 건강 정보를 조회합니다.
     * </p>
     * <ul>
     * <li>성공: 200 OK와 사용자 정보 반환</li>
     * </ul>
     *
     * @param userPrincipal 인증된 사용자 정보
     * @return 마이페이지 응답 정보
     */
    @GetMapping
    public ResTemplate<MyPageResponse> getMyPage(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        MyPageResponse response = userService.getMyPage(userPrincipal.getUserId());
        return ResTemplate.success(HttpStatus.OK, "마이페이지 조회 성공", response);
    }

    /**
     * 개인정보 수정 API
     * <p>
     * - PATCH /user
     * - 닉네임, 소속, 신체정보 등을 수정합니다.
     * </p>
     *
     * @param userPrincipal 인증된 사용자
     * @param request       수정할 정보 (Optional fields)
     * @return 수정 성공 메시지
     */
    @PatchMapping
    public ResTemplate<Void> updateUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid UserUpdateRequest request) {
        userService.updateUser(userPrincipal.getUserId(), request);
        return ResTemplate.success(HttpStatus.OK, "수정 성공");
    }

    /**
     * 식단 중점 수정 API
     * <p>
     * - PATCH /user/diet
     * - 식단 목표(Focus) 및 관련 건강 정보를 수정합니다.
     * </p>
     *
     * @param userPrincipal 인증된 사용자
     * @param request       수정할 정보 (Focus 필수)
     * @return 수정 성공 메시지
     */
    @PatchMapping("/diet")
    public ResTemplate<Void> updateDietFocus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid FocusUpdateRequest request) {
        userService.updateDietFocus(userPrincipal.getUserId(), request);
        return ResTemplate.success(HttpStatus.OK, "식단 중점 설정 수정 성공");
    }
}
