package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.global.common.dto.response.PageInfo;

public record FoodListResponse(
	List<FoodItem> foods,
	PageInfo pageInfo
) {
	public static FoodListResponse from(List<Food> foods, PageInfo pageInfo) {
		List<FoodItem> items = foods.stream()
			.map(FoodListResponse::toItem)
			.toList();
		return new FoodListResponse(items, pageInfo);
	}

	private static FoodItem toItem(Food food) {
		return new FoodItem(
			food.getId(),
			food.getName(),
			formatAmount(food.getBaseAmount(), food.getBaseUnit()),
			toInt(food.getKcal()),
			new Nutrients(
				toInt(food.getCarbs()),
				toInt(food.getProtein()),
				toInt(food.getFat())
			),
			food.getThumbnailUrl()
		);
	}

	private static String formatAmount(Integer amount, String unit) {
		if (amount == null || unit == null) {
			return "";
		}
		return amount + unit;
	}

	private static int toInt(BigDecimal value) {
		if (value == null) {
			return 0;
		}
		return value.setScale(0, RoundingMode.HALF_UP).intValue();
	}

	public record FoodItem(
		Long foodId,
		String name,
		String amount,
		int calorie,
		Nutrients nutrients,
		String thumbnailUrl
	) {
	}

	public record Nutrients(
		int carbs,
		int protein,
		int fat
	) {
	}
}
