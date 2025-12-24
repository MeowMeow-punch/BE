package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.math.BigDecimal;

// 영양소 상세 값 응답 DTO
public record NutritionDetail(
	BigDecimal current,
	int goal,
	String unit
) {
	public static NutritionDetail of(BigDecimal current, int goal, String unit) {
		return new NutritionDetail(current, goal, unit);
	}
}
