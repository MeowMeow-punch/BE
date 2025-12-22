package MeowMeowPunch.pickeat.welstory.gateway;

import java.util.List;

import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;

/**
 * [Welstory][Gateway] 캐시 데코레이터.
 *
 * <pre>
 * [Service] ──▶ [GatewayCached] ──(Cache Miss)──▶ [GatewayDirect]
 * </pre>
 *
 * - Redis/Local 캐싱 처리
 */
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
