package MeowMeowPunch.pickeat.domain.community.dto.response;

import java.time.LocalDateTime;
import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;

/**
 * [Community][Response] CommunitySummaryDto
 * 목록 조회 시 사용되는 게시글 요약 정보
 * <p>
 * 실무적 포인트:
 * - 성능 최적화를 위해 content(LOB) 제외
 * - 썸네일, 좋아요 수, 미리보기 텍스트 등 핵심 메타데이터만 포함
 * </p>
 */
public record CommunitySummaryDto(
	Long id,
	CommunityCategory category,
	String title,
	String previewText,
	String thumbnailUrl,
	int likes,
	String writer,
	String readingTime,
	LocalDateTime createdAt
) {
	public static CommunitySummaryDto from(Community community) {
		return new CommunitySummaryDto(
			community.getId(),
			community.getCategory(),
			community.getTitle(),
			community.getPreviewText(),
			community.getThumbnailUrl(),
			community.getLikes(),
			community.getWriter(),
			community.getReadingTime(),
			community.getCreatedAt()
		);
	}
}
