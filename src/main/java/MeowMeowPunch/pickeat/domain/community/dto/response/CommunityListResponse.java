package MeowMeowPunch.pickeat.domain.community.dto.response;

import java.util.List;
import MeowMeowPunch.pickeat.global.common.dto.PageInfo;

/**
 * [Community][Response] CommunityListResponse
 * 커뮤니티 게시글 목록 조회 결과 (Cursor Pagination)
 *
 * @param posts    조회된 게시글 요약 목록
 * @param pageInfo 페이지네이션 메타 정보 (다음 커서, 다음 페이지 여부)
 */
public record CommunityListResponse(
	List<CommunitySummaryDto> posts,
	PageInfo pageInfo
) {
	public static CommunityListResponse of(List<CommunitySummaryDto> posts, String nextCursor, boolean hasNext) {
		return new CommunityListResponse(posts, new PageInfo(nextCursor, hasNext));
	}
}
