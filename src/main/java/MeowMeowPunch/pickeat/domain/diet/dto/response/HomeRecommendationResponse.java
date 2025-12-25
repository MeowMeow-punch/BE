package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;

/**
 * [Diet][DTO] 홈 추천 응답 노출용
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HomeRecommendationResponse(
	List<Item> picks,
 	String aiFeedBack,
 	String mealType
) {
	public static HomeRecommendationResponse from(HomeRecommendationResult result) {
		List<Item> items = result.picks().stream()
			.map(Item::from)
			.toList();
		return new HomeRecommendationResponse(items, result.aiFeedBack(), result.mealType());
	}

	public record Item(
		Long recommendationId,
		String name,
		String thumbnailUrl,
		BigDecimal kcal,
		BigDecimal carbs,
		BigDecimal protein,
		BigDecimal fat
	) {
		public static Item from(FoodRecommendationCandidate c) {
			return new Item(
				c.recommendationId(),
				c.name(),
				c.thumbnailUrl(),
				c.kcal(),
				c.carbs(),
				c.protein(),
				c.fat()
			);
		}
	}
}
