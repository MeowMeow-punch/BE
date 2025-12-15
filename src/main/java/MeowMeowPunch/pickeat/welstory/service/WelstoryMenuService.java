package MeowMeowPunch.pickeat.welstory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;
import MeowMeowPunch.pickeat.welstory.gateway.WelstoryMenuGateway;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WelstoryMenuService {

	private final WelstoryMenuGateway gateway;

	// 식단 리스트를 웰스토리 포맷 그대로 조회 후 메뉴 DTO로 변환
	public List<WelstoryMenuItem> getMenus(String restaurantId, Integer dateYyyymmdd, String mealTimeId,
		String mealTimeName) {
		int targetDate = (dateYyyymmdd != null) ? dateYyyymmdd : 0;
		String timeId = (mealTimeId != null && !mealTimeId.isBlank()) ? mealTimeId : "2"; // 기본 점심
		return gateway.getMeals(restaurantId, targetDate, timeId, mealTimeName);
	}

	public List<MeowMeowPunch.pickeat.welstory.api.ApiTypes.RawMealMenuData> getNutrients(String restaurantId,
		Integer dateYyyymmdd, String mealTimeId, String hallNo, String menuCourseType) {
		int targetDate = (dateYyyymmdd != null) ? dateYyyymmdd : 0;
		String timeId = (mealTimeId != null && !mealTimeId.isBlank()) ? mealTimeId : "2";
		return gateway.getNutrients(restaurantId, targetDate, timeId, hallNo, menuCourseType);
	}
}
