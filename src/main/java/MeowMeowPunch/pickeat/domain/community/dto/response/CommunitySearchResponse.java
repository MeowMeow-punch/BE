package MeowMeowPunch.pickeat.domain.community.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;

public record CommunitySearchResponse(
	Long searchNum,
	List<SearchPostDetail> posts
) {
	public static CommunitySearchResponse of(List<SearchPostDetail> posts) {
		return new CommunitySearchResponse((long) posts.size(), posts);
	}

	public record SearchPostDetail(
		Long postId,
		String title,
		String thumbnailUrl,
		CommunityCategory category,
		String readingTime,
		String previewText,
		String writer,
		int likes,
		LocalDateTime createdAt
	) {
		public static SearchPostDetail from(Community community) {
			return new SearchPostDetail(
				community.getId(),
				community.getTitle(),
				community.getThumbnailUrl(),
				community.getCategory(),
				community.getReadingTime(),
				community.getPreviewText(),
				community.getWriter(),
				community.getLikes(),
				community.getCreatedAt()
			);
		}
	}
}
