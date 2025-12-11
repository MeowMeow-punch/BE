package MeowMeowPunch.pickeat.domain.diet.dto;

import java.math.BigDecimal;

public record NutrientTotals(
	BigDecimal totalKcal,
	BigDecimal totalCarbs,
	BigDecimal totalProtein,
	BigDecimal totalFat
) {
}
