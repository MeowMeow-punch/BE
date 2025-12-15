package MeowMeowPunch.pickeat.domain.diet.dto.response;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietInfo;

// 식단 상세 응답 DTO
public record DietDetailResponse(
	DietInfo dietInfo
) {
	public static DietDetailResponse from(DietInfo dietInfo) {
		return new DietDetailResponse(dietInfo);
	}
}
