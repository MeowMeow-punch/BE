package MeowMeowPunch.pickeat.welstory.gateway;

import java.util.List;

import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;

// 웰스토리 메뉴/영양 조회 진입점 (캐시 교체용 인터페이스)
public interface WelstoryMenuGateway {
	List<WelstoryMenuItem> getMeals(String restaurantId, int dateYyyymmdd, String mealTimeId, String mealTimeName);

	List<ApiTypes.RawMealMenuData> getNutrients(String restaurantId, int dateYyyymmdd, String mealTimeId, String hallNo,
		String menuCourseType);
}
