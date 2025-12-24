package MeowMeowPunch.pickeat.welstory.gateway;

import java.util.List;

import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;

/**
 * [Welstory][Gateway] 메뉴 조회 게이트웨이 인터페이스.
 */
public interface WelstoryMenuGateway {
	List<WelstoryMenuItem> getMeals(String restaurantId, int dateYyyymmdd, String mealTimeId, String mealTimeName);

	List<ApiTypes.RawMealMenuData> getNutrients(String restaurantId, int dateYyyymmdd, String mealTimeId, String hallNo,
		String menuCourseType);
}
