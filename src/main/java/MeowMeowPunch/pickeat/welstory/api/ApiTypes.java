package MeowMeowPunch.pickeat.welstory.api;

import java.util.List;

public final class ApiTypes {
	private ApiTypes() {
	}

	// ===== 식당 검색 =====
	public record RestaurantSearchResponse(List<RawRestaurantData> data) {
	}

	public record RawRestaurantData(
		@com.fasterxml.jackson.annotation.JsonAlias({"restaurantId", "restaurantCode"})
		String restaurantId,
		String restaurantName,
		String restaurantDesc
	) {
	}

	// ===== 내 식당 =====
	public record RegisteredRestaurantsResponse(List<RegisteredRestaurantData> data) {
	}

	public record RegisteredRestaurantData(String restaurantId) {
	}

	// ===== 식사 시간대 =====
	public record MealTimeResponse(List<MealTimeData> data) {
	}

	public record MealTimeData(
		String code,
		String codeNm
	) {
	}

	// ===== 식단 리스트 =====
	public record MealListResponse(MealListData data) {
	}

	public record MealListData(
		List<RawMealData> mealList
	) {
	}

	public record RawMealData(
		String hallNo,
		String menuName,
		String courseTxt,
		String menuCourseType,
		String setMenuName,
		String subMenuTxt,
		String photoUrl,
		String photoCd,
		String sumKcal,
		String menuMealTypeTxt
	) {
	}

	// ===== 영양 =====
	public record MealNutrientResponse(List<RawMealMenuData> data) {
	}

	public record RawMealMenuData(
		String menuName,
		String typicalMenu, // "Y" | "N"
		String kcal,
		String totCho,
		String totSugar,
		String totFib,
		String totFat,
		String totProtein
	) {
	}

	// ===== 세션 =====
	public record SessionRefreshResponse(String data) {
	}
}
