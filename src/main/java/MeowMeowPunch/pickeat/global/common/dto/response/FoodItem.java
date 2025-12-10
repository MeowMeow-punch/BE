package MeowMeowPunch.pickeat.global.common.dto.response;

// 단일 음식 카드/목록에 내려가는 공통 응답 DTO

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
