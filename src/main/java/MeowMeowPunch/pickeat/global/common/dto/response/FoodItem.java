package MeowMeowPunch.pickeat.global.common.dto.response;

// 단일 음식 목록에 보여주는 공통 응답 DTO
public record FoodItem(
	Long foodId,
	String name,
	int amount,
	int calorie,
	Nutrients nutrients,
	String thumbnailUrl
) {
	public static FoodItem of(Long foodId, String name, int amount, int calorie, Nutrients nutrients,
		String thumbnailUrl) {
		return new FoodItem(foodId, name, amount, calorie, nutrients, thumbnailUrl);
	}
}
