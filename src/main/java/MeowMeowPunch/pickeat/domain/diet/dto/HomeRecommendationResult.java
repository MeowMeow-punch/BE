package MeowMeowPunch.pickeat.domain.diet.dto;

import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;

// 홈 추천 결과 DTO
public record HomeRecommendationResult(
	List<FoodRecommendationCandidate> picks,
	String reason
) {
	public static HomeRecommendationResult of(List<FoodRecommendationCandidate> picks, String reason) {
		return new HomeRecommendationResult(picks, reason);
	}

	public static HomeRecommendationResult empty(String reason) {
		return new HomeRecommendationResult(List.of(), reason);
	}
}
