package MeowMeowPunch.pickeat.global.common.dto.response;

// 추천 식단 카드 공통 DTO
public record RecommendedDietInfo(
	Long dietId,
	String name,
	String mealType,
	String thumbnailUrl,
	int calorie
) {
	public static RecommendedDietInfo of(Long dietId, String name, String mealType, String thumbnailUrl, int calorie) {
		return new RecommendedDietInfo(dietId, name, mealType, thumbnailUrl, calorie);
	}
}
