package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// 식단 상세 - 음식 정보 DTO
public record DietDetailItem(
	Long foodId,
	String name,
	int amount,
	int quantity,
	int calorie,
	Nutrients nutrients,
	String thumbnailUrl
) {
	public static DietDetailItem from(FoodItem item, int quantity) {
		return new DietDetailItem(
			item.foodId(),
			item.name(),
			item.amount(),
			quantity,
			item.calorie() * quantity,
			Nutrients.of(
				item.nutrients().carbs() * quantity,
				item.nutrients().protein() * quantity,
				item.nutrients().fat() * quantity
			),
			item.thumbnailUrl()
		);
	}
}
