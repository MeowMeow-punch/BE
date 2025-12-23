package MeowMeowPunch.pickeat.domain.community.dto.response;

/**
 * [Community][Response] CommunityLikeResponse
 * 좋아요 토글 동작 후 반환되는 최신 상태
 *
 * @param likes 갱신된 좋아요 총 개수
 * @param isLiked 갱신된 본인의 좋아요 상태
 */
public record CommunityLikeResponse(
	int likes,
	boolean isLiked
) {
}
