package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;

// 식단 메인 응답 DTO
public record DietHomeResponse(
	SummaryInfo summaryInfo,
	AiFeedBack aiFeedbackInfo,
	List<RecommendedDietInfo> recommendedDietsInfo
) {
	public static DietHomeResponse of(SummaryInfo summaryInfo, AiFeedBack aiFeedbackInfo,
		List<RecommendedDietInfo> recommendedDietsInfo) {
		return new DietHomeResponse(summaryInfo, aiFeedbackInfo, recommendedDietsInfo);
	}
}
