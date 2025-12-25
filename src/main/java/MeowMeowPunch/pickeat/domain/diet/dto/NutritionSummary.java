package MeowMeowPunch.pickeat.domain.diet.dto;

import java.util.Collections;
import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.NutrientCode;

/**
 * [Diet][DTO] 영양 상태 요약(과잉/부족/적정)
 */
public record NutritionSummary(
	List<NutrientCode> criticalExcess,
	List<NutrientCode> moderateExcess,
	List<NutrientCode> deficit,
	List<NutrientCode> adequate
) {
	public static NutritionSummary empty() {
		return new NutritionSummary(
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList()
		);
	}
}
