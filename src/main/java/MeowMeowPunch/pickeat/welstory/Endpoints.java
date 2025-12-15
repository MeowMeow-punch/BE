package MeowMeowPunch.pickeat.welstory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class Endpoints {
	private Endpoints() {
	}

	public static final String LOGIN = "/login";
	public static final String SESSION_REFRESH = "/session";

	public static String searchRestaurant(String query) {
		return "/api/mypage/rest-list?restaurantName=" +
			URLEncoder.encode(query, StandardCharsets.UTF_8);
	}

	public static final String LIST_MY_RESTAURANT = "/api/mypage/rest-my-list";
	public static final String REGISTER_MY_RESTAURANT = "/api/mypage/rest-regi";
	public static final String DELETE_MY_RESTAURANT = "/api/mypage/rest-delete";

	public static final String LIST_MEAL_TIME = "/api/menu/getMealTimeList";

	public static String listMeal(int dateYyyymmdd, String mealTimeId, String restaurantId) {
		return "/api/meal?menuDt=" + dateYyyymmdd +
			"&menuMealType=" + mealTimeId +
			"&restaurantCode=" + restaurantId;
	}

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
