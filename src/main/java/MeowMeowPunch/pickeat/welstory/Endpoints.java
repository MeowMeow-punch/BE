package MeowMeowPunch.pickeat.welstory;

// Welstory API 엔드포인트/쿼리 문자열을 조립하는 유틸
public final class Endpoints {
	private Endpoints() {
	}

	public static final String LOGIN = "/login";

	public static final String LIST_MEAL_TIME = "/api/menu/getMealTimeList";

	// 특정 날짜·식사시간대의 식단 목록 URI 생성
	public static String listMeal(int dateYyyymmdd, String mealTimeId, String restaurantId) {
		return "/api/meal?menuDt=" + dateYyyymmdd +
			"&menuMealType=" + mealTimeId +
			"&restaurantCode=" + restaurantId;
	}

	// 특정 식단의 영양 정보 URI 생성
	public static String listMealNutrient(
		int dateYyyymmdd,
		String mealTimeId,
		String hallNo,
		String menuCourseType,
		String restaurantId
	) {
		return "/api/meal/detail/nutrient?menuDt=" + dateYyyymmdd +
			"&hallNo=" + hallNo +
			"&menuCourseType=" + menuCourseType +
			"&menuMealType=" + mealTimeId +
			"&restaurantCode=" + restaurantId;
	}
}
