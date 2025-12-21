package MeowMeowPunch.pickeat.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import MeowMeowPunch.pickeat.domain.user.dto.response.UserGroupResponse;
import MeowMeowPunch.pickeat.domain.user.service.UserService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;

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
    public ResponseEntity<ResTemplate<Void>> checkNickname(@RequestParam String nickname) {
        userService.checkNickname(nickname);
        return ResponseEntity.ok(
                new ResTemplate<>(HttpStatus.OK, "해당 닉네임은 사용가능합니다.", null));
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
    public ResponseEntity<ResTemplate<List<UserGroupResponse>>> searchGroups(@RequestParam String keyword) {
        List<UserGroupResponse> groups = userService.searchGroups(keyword);

        return ResponseEntity.ok(
                new ResTemplate<>(HttpStatus.OK, "그룹명 조회 성공", groups));
    }
}
