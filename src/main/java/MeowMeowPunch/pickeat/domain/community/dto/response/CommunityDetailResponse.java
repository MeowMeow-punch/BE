package MeowMeowPunch.pickeat.domain.community.dto.response;

import java.time.LocalDateTime;
import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;

/**
 * [Community][Response] CommunityDetailResponse
 * 커뮤니티 게시글 상세 조회 결과
 *
 * @param content 게시글 본문 (TEXT) - 상세 조회 시에만 포함
 * @param userId 작성자 식별자 (작성자 본인 여부 확인용)
 * @param isLiked 요청자(로그인 User)의 좋아요 여부
 */
public record CommunityDetailResponse(
	Long id,
	CommunityCategory category,
	String title,
	String writer,
	String userId,
	String readingTime,
	int likes,
	boolean isLiked,
	String thumbnailUrl,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static CommunityDetailResponse of(Community community, boolean isLiked) {
		return new CommunityDetailResponse(
			community.getId(),
			community.getCategory(),
			community.getTitle(),
			community.getWriter(),
			community.getUserId(),
			community.getReadingTime(),
			community.getLikes(),
			isLiked,
			community.getThumbnailUrl(),
			community.getContent(),
			community.getCreatedAt(),
			community.getUpdatedAt()
		);
	}
}
