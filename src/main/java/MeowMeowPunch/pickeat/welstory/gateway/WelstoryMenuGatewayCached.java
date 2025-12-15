package MeowMeowPunch.pickeat.welstory.gateway;

import java.util.List;

import MeowMeowPunch.pickeat.welstory.api.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;

// TODO: Caffeine/Redis 캐시 적용 시 구현 예정
public class WelstoryMenuGatewayCached implements WelstoryMenuGateway {
	@Override
	public List<WelstoryMenuItem> getMeals(String restaurantId, int dateYyyymmdd, String mealTimeId,
		String mealTimeName) {
		throw new UnsupportedOperationException("Caching gateway not implemented yet");
	}

	@Override
	public List<ApiTypes.RawMealMenuData> getNutrients(String restaurantId, int dateYyyymmdd, String mealTimeId,
		String hallNo, String menuCourseType) {
		throw new UnsupportedOperationException("Caching gateway not implemented yet");
	}
}
