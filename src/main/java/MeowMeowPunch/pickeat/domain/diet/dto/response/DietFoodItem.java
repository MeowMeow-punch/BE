package MeowMeowPunch.pickeat.domain.diet.dto.response;

import MeowMeowPunch.pickeat.global.common.dto.response.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.Nutrients;

// 식단 상세 - 음식 정보 DTO
public record DietFoodItem(
	Long foodId,
	String name,
	int amount,
	int quantity,
	int calorie,
	Nutrients nutrients,
	String thumbnailUrl
) {
	public static DietFoodItem from(FoodItem item, int quantity) {
		return new DietFoodItem(
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
