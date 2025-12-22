package MeowMeowPunch.pickeat.domain.community.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * [Community][Response] CommunityListResponse
 * 커뮤니티 게시글 목록 조회 결과 (Paging 포함)
 *
 * @param contents   조회된 게시글 요약 목록
 * @param totalPages 전체 페이지 수
 * @param totalElements 전체 게시글 수
 * @param currentPage 현재 페이지 번호 (0-based)
 */
public record CommunityListResponse(
	List<CommunitySummaryDto> contents,
	int totalPages,
	long totalElements,
	int currentPage
) {
	public static CommunityListResponse of(Page<CommunitySummaryDto> page) {
		return new CommunityListResponse(
			page.getContent(),
			page.getTotalPages(),
			page.getTotalElements(),
			page.getNumber()
		);
	}
}
