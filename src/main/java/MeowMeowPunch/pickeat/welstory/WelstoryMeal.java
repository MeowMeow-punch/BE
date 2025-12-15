package MeowMeowPunch.pickeat.welstory;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;

import MeowMeowPunch.pickeat.welstory.api.ApiTypes;
import MeowMeowPunch.pickeat.welstory.api.WelstoryResponse;

public class WelstoryMeal {

	private final WelstoryClient client;

	public final String restaurantId;
	public final int dateYyyymmdd;
	public final String mealTimeId;
	public final String hallNo;
	public final String menuCourseType;

	public WelstoryMeal(WelstoryClient client, String restaurantId, int dateYyyymmdd,
		String mealTimeId, String hallNo, String menuCourseType) {
		this.client = client;
		this.restaurantId = restaurantId;
		this.dateYyyymmdd = dateYyyymmdd;
		this.mealTimeId = mealTimeId;
		this.hallNo = hallNo;
		this.menuCourseType = menuCourseType;
	}

	// 메뉴 영양 정보 원본 조회
	public List<ApiTypes.RawMealMenuData> listNutrientsRaw() {
		client.ensureToken();

		String normalizedHallNo = trimSafe(hallNo);
		String normalizedMenuCourseType = trimSafe(menuCourseType);
		String normalizedMealTimeId = trimSafe(mealTimeId);

		var res = client.callWithRetry(
			() -> client.http().get(
				Endpoints.listMealNutrient(dateYyyymmdd, normalizedMealTimeId, normalizedHallNo, normalizedMenuCourseType,
					restaurantId),
				Map.of("Cookie", "cafeteriaActiveId=" + restaurantId),
				new ParameterizedTypeReference<WelstoryResponse<List<ApiTypes.RawMealMenuData>>>() {
				}
			)
		);

		return client.unwrap(res, "영양 정보 조회").data();
	}

	private String trimSafe(String value) {
		return value == null ? null : value.trim();
	}
}
