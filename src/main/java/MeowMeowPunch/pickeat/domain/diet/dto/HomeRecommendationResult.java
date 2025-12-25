package MeowMeowPunch.pickeat.domain.diet.dto;

import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;

// 홈 추천 결과 DTO
public record HomeRecommendationResult(
	List<FoodRecommendationCandidate> picks,
	String aiFeedBack,
	String mealType
) {
	public static HomeRecommendationResult of(List<FoodRecommendationCandidate> picks, String aiFeedBack,
		String mealType) {
		return new HomeRecommendationResult(picks, aiFeedBack, mealType);
	}

	public static HomeRecommendationResult empty(String aiFeedBack) {
		return new HomeRecommendationResult(List.of(), aiFeedBack, null);
	}
}
