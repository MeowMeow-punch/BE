package MeowMeowPunch.pickeat.domain.diet.dto;

import java.util.List;

/**
 * 홈 추천 결과(선택된 후보 + 한 줄 이유)
 */
public record HomeRecommendationResult(
	List<FoodRecommendationCandidate> picks,
	String reason,
	boolean aiUsed
) {
	public static HomeRecommendationResult of(List<FoodRecommendationCandidate> picks, String reason, boolean aiUsed) {
		return new HomeRecommendationResult(picks, reason, aiUsed);
	}

	public static HomeRecommendationResult empty(String reason) {
		return new HomeRecommendationResult(List.of(), reason, false);
	}
}
