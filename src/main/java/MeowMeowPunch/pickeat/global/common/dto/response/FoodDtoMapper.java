package MeowMeowPunch.pickeat.global.common.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import MeowMeowPunch.pickeat.global.common.enums.FoodBaseUnit;

// Food 엔티티/조회 DTO 를 API 응답용 FoodItem 으로 변환하는 매퍼
public final class FoodDtoMapper {
	private FoodDtoMapper() {
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
