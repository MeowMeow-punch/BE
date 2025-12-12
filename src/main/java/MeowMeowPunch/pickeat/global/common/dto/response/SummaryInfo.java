package MeowMeowPunch.pickeat.global.common.dto.response;

import MeowMeowPunch.pickeat.global.common.enums.DietStatus;

// 하루 식단 요약(칼로리, 탄단지 목표 대비 현황) 응답 공통 DTO
public record SummaryInfo(
	Calorie calorie, NutrientInfo carbs, NutrientInfo protein, NutrientInfo fat
) {
	public static SummaryInfo of(
		Calorie calorie, NutrientInfo carbs, NutrientInfo protein, NutrientInfo fat
	) {
		return new SummaryInfo(calorie, carbs, protein, fat);
	}

	public record Calorie(
		int current, int goal
	) {
		public static Calorie of(int current, int goal) {
			return new Calorie(current, goal);
		}
	}

	public record NutrientInfo(
		int current,
		int goal,
		DietStatus status
	) {
		public static NutrientInfo of(int current, int goal, DietStatus status) {
			return new NutrientInfo(current, goal, status);
		}
	}
}
