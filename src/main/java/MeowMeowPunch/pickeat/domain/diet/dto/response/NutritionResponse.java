package MeowMeowPunch.pickeat.domain.diet.dto.response;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.NutritionInfo;

// 영양분 상세 조회 응답 DTO
public record NutritionResponse(
	NutritionInfo nutritionInfo
) {
	public static NutritionResponse of(NutritionInfo nutritionInfo) {
		return new NutritionResponse(nutritionInfo);
	}
}
