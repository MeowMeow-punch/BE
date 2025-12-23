package MeowMeowPunch.pickeat.domain.community.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import MeowMeowPunch.pickeat.domain.community.dto.request.CommunityLikeRequest;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityDetailResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityListResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunitySearchResponse;
import MeowMeowPunch.pickeat.domain.community.service.CommunityService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import MeowMeowPunch.pickeat.global.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;

/**
 * [Community][Controller] CommunityController
 * 커뮤니티 관련 API 엔드포인트
 */
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

	private final CommunityService communityService;

	private static final String DEFAULT_PAGE_SIZE = "5";

	/**
	 * 커뮤니티 게시글 목록 조회 (Cursor Pagination)
	 *
	 * @param category 조회할 카테고리 (path variable)
	 * @param cursorId 이전 페이지의 마지막 게시글 ID (첫 조회 시 생략 가능)
	 * @param size     페이지 크기 (Default: 5)
	 * @return 커뮤니티 목록 응답
	 */
	@GetMapping("/{category}")
	public ResTemplate<CommunityListResponse> getCommunityList(
		@PathVariable(name = "category") String category,
		@RequestParam(name = "cursorId", required = false) Long cursorId,
		@RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE) int size
	) {
		CommunityListResponse response = communityService.getCommunityList(category, cursorId, size);
		return ResTemplate.success(HttpStatus.OK, "컨텐츠 목록 조회 성공", response);
	}

	/**
	 * 커뮤니티 게시글 상세 조회
	 * <p>
	 * {communityId}가 숫자일 경우 이 메서드가 우선 매핑됩니다.
	 * </p>
	 *
	 * @param communityId 게시글 ID
	 * @param principal   요청 사용자 정보 (Nullable)
	 * @return 게시글 상세 정보 (ResTemplate Wrapped)
	 */
	@GetMapping("/{communityId:\\d+}")
	public ResTemplate<CommunityDetailResponse> getCommunityDetail(
		@PathVariable(name = "communityId") Long communityId,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		CommunityDetailResponse response = communityService.getCommunityDetail(communityId, principal);
		return ResTemplate.success(HttpStatus.OK, "컨텐츠 상세 조회 성공", response);
	}

	/**
	 * 커뮤니티 게시글 검색
	 *
	 * @param keyword 검색어 (제목 or 본문 포함)
	 * @return 검색 결과 (게시글 리스트 + 개수)
	 */
	@GetMapping("/search")
	public ResTemplate<CommunitySearchResponse> getCommunitySearch(
		@RequestParam(name = "keyword") String keyword
	) {
		CommunitySearchResponse response = communityService.searchCommunity(keyword);
		return ResTemplate.success(HttpStatus.OK, "컨텐츠 검색 성공", response);
	}

	/**
	 * 커뮤니티 게시글 좋아요 상태 변경 (토글 아님, 상태 동기화)
	 *
	 * @param communityId 게시글 ID
	 * @param principal   요청 사용자 정보 (Required)
	 * @param request     변경할 좋아요 상태 (isLiked)
	 * @return 성공 메시지
	 */
	@PostMapping("/{communityId:\\d+}/likes")
	public ResTemplate<Void> updateLikeStatus(
		@PathVariable(name = "communityId") Long communityId,
		@AuthenticationPrincipal UserPrincipal principal,
		@Valid @RequestBody CommunityLikeRequest request
	) {
		communityService.updateLikeStatus(communityId, principal, request);
		return ResTemplate.success(HttpStatus.OK, "좋아요 상태변경 성공");
	}
}
