package MeowMeowPunch.pickeat.global.common.dto.response;

import java.math.BigDecimal;

import MeowMeowPunch.pickeat.global.common.enums.FoodBaseUnit;

// MyBatis 조회 결과를 나타내기 위한 음식 요약 DTO
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
