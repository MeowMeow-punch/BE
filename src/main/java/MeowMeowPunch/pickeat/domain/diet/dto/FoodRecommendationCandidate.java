package MeowMeowPunch.pickeat.domain.diet.dto;

import java.math.BigDecimal;

// 음식 추천 후보
public record FoodRecommendationCandidate(
	Long foodId,
	String name,
	String thumbnailUrl,
	BigDecimal kcal,
	BigDecimal carbs,
	BigDecimal protein,
	BigDecimal fat,
	String category,
	double score
) {
}
