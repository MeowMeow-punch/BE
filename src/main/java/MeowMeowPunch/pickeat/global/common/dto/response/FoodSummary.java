package MeowMeowPunch.pickeat.global.common.dto.response;

import java.math.BigDecimal;

public record FoodSummary(
	Long id,
	String name,
	Integer baseAmount,
	String baseUnit,
	BigDecimal kcal,
	BigDecimal carbs,
	BigDecimal protein,
	BigDecimal fat,
	String thumbnailUrl
) {
}
