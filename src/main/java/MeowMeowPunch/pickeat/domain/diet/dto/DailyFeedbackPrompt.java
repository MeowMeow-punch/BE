package MeowMeowPunch.pickeat.domain.diet.dto;

import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;

/**
 * [Diet][DTO] Daily Nutrition Feedback 프롬프트 입력 JSON
 */
public record DailyFeedbackPrompt(
	boolean isFirstMeal,
	DietType mealSlot,
	Focus focus,
	NutritionSummary nutritionSummary
) {
}
