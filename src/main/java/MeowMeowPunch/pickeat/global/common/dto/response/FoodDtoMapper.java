package MeowMeowPunch.pickeat.global.common.dto.response;

// Food 엔티티/조회 DTO를 API 응답용 FoodItem으로 변환하는 매퍼

import java.math.BigDecimal;
import java.math.RoundingMode;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.global.common.enums.FoodBaseUnit;

// 음식
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

	private static String formatAmount(Integer amount, FoodBaseUnit unit) {
		if (amount == null || unit == null) {
			return "";
		}
		return amount + unit.name();
	}

	private static int toInt(BigDecimal value) {
		if (value == null) {
			return 0;
		}
		return value.setScale(0, RoundingMode.HALF_UP).intValue();
	}

}
