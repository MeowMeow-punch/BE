package MeowMeowPunch.pickeat.domain.community.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;

/**
 * [Community][Response] CommunityDetailResponse
 * <p>
 * 커뮤니티 게시글 상세 조회 결과 DTO
 * </p>
 * <ul>
 *   <li><b>post</b>: 게시글 상세 정보</li>
 *   <li><b>relatedPosts</b>: 연관 게시글 (동일 카테고리 최신글 3개)</li>
 * </ul>
 */
public record CommunityDetailResponse(
	PostDetail post,
	List<RelatedPostResponse> relatedPosts
) {
	public static CommunityDetailResponse of(Community community, boolean isLiked, List<RelatedPostResponse> relatedPosts) {
		return new CommunityDetailResponse(
			PostDetail.from(community, isLiked),
			relatedPosts
		);
	}

	public record PostDetail(
		Long postId,
		String title,
		CommunityCategory category,
		LocalDateTime createdAt,
		int likes,
		String thumbnailUrl,
		String writer,
		String content,
		boolean isLiked
	) {
		public static PostDetail from(Community community, boolean isLiked) {
			return new PostDetail(
				community.getId(),
				community.getTitle(),
				community.getCategory(),
				community.getCreatedAt(),
				community.getLikes(),
				community.getThumbnailUrl(),
				community.getWriter(),
				community.getContent(),
				isLiked
			);
		}
	}

	public record RelatedPostResponse(
		Long id,
		String title,
		String thumbnailUrl,
		CommunityCategory category,
		String previewText
	) {
		public static RelatedPostResponse from(Community community) {
			return new RelatedPostResponse(
				community.getId(),
				community.getTitle(),
				community.getThumbnailUrl(),
				community.getCategory(),
				community.getPreviewText()
			);
		}
	}
}
