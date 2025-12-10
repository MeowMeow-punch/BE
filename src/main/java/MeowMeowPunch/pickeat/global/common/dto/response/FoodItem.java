package MeowMeowPunch.pickeat.global.common.dto.response;

public record FoodItem(
	Long foodId,
	String name,
	String amount,
	int calorie,
	Nutrients nutrients,
	String thumbnailUrl
) {
	public static FoodItem of(Long foodId, String name, String amount, int calorie, Nutrients nutrients,
		String thumbnailUrl) {
		return new FoodItem(foodId, name, amount, calorie, nutrients, thumbnailUrl);
	}
}
