package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

// 추천 식단 상세 응답용 DTO (recommendationId 필드 사용)
public record RecommendationInfo(
	Long recommendationId,
	String mealType,
	String time,
	String date,
	List<DietDetailItem> foods
) {
	public static RecommendationInfo of(Long recommendationId, String mealType, String time, String date,
		List<DietDetailItem> foods) {
		return new RecommendationInfo(recommendationId, mealType, time, date, foods);
	}
}
