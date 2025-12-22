package MeowMeowPunch.pickeat.domain.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityListResponse;
import MeowMeowPunch.pickeat.domain.community.service.CommunityService;
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
	public ResponseEntity<CommunityListResponse> getCommunityList(
		@PathVariable(name = "category") String category,
		@RequestParam(name = "cursorId", required = false) Long cursorId,
		@RequestParam(name = "size", defaultValue = DEFAULT_PAGE_SIZE) int size
	) {
		CommunityListResponse response = communityService.getCommunityList(category, cursorId, size);
		return ResponseEntity.ok(response);
	}
}
