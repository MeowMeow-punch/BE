package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.math.BigDecimal;
import java.math.RoundingMode;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;

// Food 엔티티/조회 DTO 를 API 응답용 FoodItem 으로 변환하는 매퍼
public final class FoodDtoMapper {
	private FoodDtoMapper() {
	}

	public static FoodItem toFoodItem(FoodSummary summary) {
		return FoodItem.of(
			summary.id(),
			summary.name(),
			toAmount(summary.baseAmount()),
			toInt(summary.kcal()),
			Nutrients.of(
				toInt(summary.carbs()),
				toInt(summary.protein()),
				toInt(summary.fat())
			),
			summary.thumbnailUrl()
		);
	}

	public static FoodItem toFoodItem(Food food) {
		return FoodItem.of(
			food.getId(),
			food.getName(),
			toAmount(food.getBaseAmount()),
			toInt(food.getKcal()),
			Nutrients.of(
				toInt(food.getCarbs()),
				toInt(food.getProtein()),
				toInt(food.getFat())
			),
			food.getThumbnailUrl()
		);
	}

	private static int toAmount(Integer amount) {
		if (amount == null) {
			return 0;
		}
		return amount;
	}

	private static int toInt(BigDecimal value) {
		if (value == null) {
			return 0;
		}
		return value.setScale(0, RoundingMode.HALF_UP).intValue();
	}

}
