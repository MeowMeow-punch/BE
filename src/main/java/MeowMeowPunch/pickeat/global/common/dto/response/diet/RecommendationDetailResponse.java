package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// 추천 식단 상세 응답 DTO (필드명 recommendationId 사용)
public record RecommendationDetailResponse(
	RecommendationInfo dietInfo
) {
	public static RecommendationDetailResponse from(RecommendationInfo dietInfo) {
		return new RecommendationDetailResponse(dietInfo);
	}
}
