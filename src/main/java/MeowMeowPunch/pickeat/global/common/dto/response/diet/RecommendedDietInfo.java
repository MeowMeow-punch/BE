package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

// 추천 식단 카드 공통 DTO
public record RecommendedDietInfo(
	Long recommendationId,
	String name,
	String mealType,
	List<String> thumbnailUrls,
	int calorie
) {
	public static RecommendedDietInfo of(Long recommendationId, String name, String mealType, List<String> thumbnailUrls,
		int calorie) {
		return new RecommendedDietInfo(recommendationId, name, mealType, thumbnailUrls, calorie);
	}
}
