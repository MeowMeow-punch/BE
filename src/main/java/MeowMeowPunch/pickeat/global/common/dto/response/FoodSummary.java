package MeowMeowPunch.pickeat.global.common.dto.response;

// MyBatis 조회 결과를 표현하는 음식 요약 DTO (필요 필드만 포함)

import java.math.BigDecimal;

import MeowMeowPunch.pickeat.global.common.enums.FoodBaseUnit;

public record FoodSummary(
	Long id,
	String name,
	Integer baseAmount,
	FoodBaseUnit baseUnit,
	BigDecimal kcal,
	BigDecimal carbs,
	BigDecimal protein,
	BigDecimal fat,
	String thumbnailUrl
) {
}
