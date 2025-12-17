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
		String normalizedTime = time;
		if (normalizedTime != null && normalizedTime.length() > 5) {
			normalizedTime = normalizedTime.substring(0, 5); // HH:mm 만 남김
		}
		return new RecommendationInfo(recommendationId, mealType, normalizedTime == null ? "" : normalizedTime, date,
			foods);
	}
}
