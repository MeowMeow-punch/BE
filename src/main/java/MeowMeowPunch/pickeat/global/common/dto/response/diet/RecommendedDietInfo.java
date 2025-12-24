package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;

// 추천 식단 카드 공통 DTO
public record RecommendedDietInfo(
	Long recommendationId,
	String name,
	String mealType,
	List<String> thumbnailUrls,
	int calorie,
	Nutrients nutrients,
	DietSourceType sourceType
) {
    public static RecommendedDietInfo of(Long recommendationId, String name, String mealType,
        List<String> thumbnailUrls, int calorie, Nutrients nutrients, DietSourceType sourceType) {
        return new RecommendedDietInfo(recommendationId, name, mealType, thumbnailUrls, calorie, nutrients, sourceType);
    }

    // Backward-compatible factory: resolve sourceType from context when not provided
    public static RecommendedDietInfo of(Long recommendationId, String name, String mealType,
        List<String> thumbnailUrls, int calorie, Nutrients nutrients) {
        DietSourceType src = RecommendedDietInfoContext.resolve(recommendationId);
        return of(recommendationId, name, mealType, thumbnailUrls, calorie, nutrients, src);
    }
}
