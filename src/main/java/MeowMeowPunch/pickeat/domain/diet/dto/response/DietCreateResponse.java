package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.dto.request.DietCreateRequest;
import MeowMeowPunch.pickeat.global.common.enums.DietType;

public record DietCreateResponse(
	Long dietId,
	String title,
	String thumbnailUrl,
	String date,
	String mealType,
	String time,
	Nutrition nutrition,
	List<FoodItem> foods
) implements DietResponse {

	public static DietCreateResponse of(
		Long dietId,
		String title,
		String thumbnailUrl,
		LocalDate date,
		DietType mealType,
		LocalTime time,
		Nutrition nutrition,
		List<DietCreateRequest.FoodQuantity> foods
	) {
		List<FoodItem> foodItems = foods.stream()
			.map(f -> new FoodItem(f.foodId(), f.quantity()))
			.toList();

		return new DietCreateResponse(
			dietId,
			title,
			thumbnailUrl,
			date.toString(),
			mealType.name(),
			time != null ? time.toString() : "",
			nutrition,
			foodItems
		);
	}

	public record FoodItem(
		Long foodId,
		Integer quantity
	) {
	}

	public record Nutrition(
		BigDecimal kcal,
		BigDecimal carbs,
		BigDecimal protein,
		BigDecimal fat,
		BigDecimal sugar,
		BigDecimal vitA,
		BigDecimal vitC,
		BigDecimal vitD,
		BigDecimal calcium,
		BigDecimal iron,
		BigDecimal dietaryFiber,
		BigDecimal sodium
	) {
		public static Nutrition of(
			BigDecimal kcal,
			BigDecimal carbs,
			BigDecimal protein,
			BigDecimal fat,
			BigDecimal sugar,
			BigDecimal vitA,
			BigDecimal vitC,
			BigDecimal vitD,
			BigDecimal calcium,
			BigDecimal iron,
			BigDecimal dietaryFiber,
			BigDecimal sodium
		) {
			return new Nutrition(
				kcal, carbs, protein, fat, sugar, vitA, vitC, vitD, calcium, iron, dietaryFiber, sodium
			);
		}
	}
}
