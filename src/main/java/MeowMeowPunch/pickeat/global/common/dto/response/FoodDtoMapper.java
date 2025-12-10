package MeowMeowPunch.pickeat.global.common.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;

public final class FoodDtoMapper {
	private FoodDtoMapper() {
	}

	public static FoodItem toFoodItem(Food food) {
		return FoodItem.of(
			food.getId(),
			food.getName(),
			formatAmount(food.getBaseAmount(), food.getBaseUnit()),
			toInt(food.getKcal()),
			Nutrients.of(
				toInt(food.getCarbs()),
				toInt(food.getProtein()),
				toInt(food.getFat())
			),
			food.getThumbnailUrl()
		);
	}

	public static FoodItem toFoodItem(FoodSummary summary) {
		return FoodItem.of(
			summary.id(),
			summary.name(),
			formatAmount(summary.baseAmount(), summary.baseUnit()),
			toInt(summary.kcal()),
			Nutrients.of(
				toInt(summary.carbs()),
				toInt(summary.protein()),
				toInt(summary.fat())
			),
			summary.thumbnailUrl()
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

}
