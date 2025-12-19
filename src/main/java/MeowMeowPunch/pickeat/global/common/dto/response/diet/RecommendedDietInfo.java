package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.MealSourceType;

// 추천 식단 카드 공통 DTO
public record RecommendedDietInfo(
	Long recommendationId,
	MealSourceType sourceType,
	boolean isEditable,
	String name,
	String mealType,
	List<String> thumbnailUrls,
	int calorie,
	Nutrients nutrients
) {
	public static RecommendedDietInfo of(Long recommendationId, MealSourceType sourceType, boolean isEditable,
		String name, String mealType, List<String> thumbnailUrls,
		int calorie, Nutrients nutrients) {
		return new RecommendedDietInfo(recommendationId, sourceType, isEditable, name, mealType, thumbnailUrls, calorie,
			nutrients);
	}
}
