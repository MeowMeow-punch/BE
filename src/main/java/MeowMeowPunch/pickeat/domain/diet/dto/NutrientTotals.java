package MeowMeowPunch.pickeat.domain.diet.dto;

import java.math.BigDecimal;

// 영양분 합계 DTO
public record NutrientTotals(
	BigDecimal totalKcal,
	BigDecimal totalCarbs,
	BigDecimal totalProtein,
	BigDecimal totalFat,
	BigDecimal totalSodium
) {
}
