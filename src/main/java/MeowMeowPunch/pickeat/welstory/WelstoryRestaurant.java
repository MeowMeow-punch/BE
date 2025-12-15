package MeowMeowPunch.pickeat.welstory;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;

import MeowMeowPunch.pickeat.welstory.api.ApiTypes;
import MeowMeowPunch.pickeat.welstory.api.WelstoryResponse;

public class WelstoryRestaurant {

	private final WelstoryClient client;

	public final String id;
	public final String name;
	public final String description;

	public WelstoryRestaurant(WelstoryClient client, String id, String name, String description) {
		this.client = client;
		this.id = id;
		this.name = name;
		this.description = description;
	}

	// 식사 시간대 원본 목록 조회
	public List<ApiTypes.MealTimeData> listMealTimesRaw() {
		client.ensureToken();

		var res = client.callWithRetry(
			() -> client.http().get(
				Endpoints.LIST_MEAL_TIME,
				Map.of("Cookie", "cafeteriaActiveId=" + id),
				new ParameterizedTypeReference<WelstoryResponse<List<ApiTypes.MealTimeData>>>() {
				}
			)
		);

		return client.unwrap(res, "식사 시간대 조회").data();
	}

	// 특정 날짜/시간대 식단 원본 목록 조회
	public List<ApiTypes.RawMealData> listMealsRaw(int dateYyyymmdd, String mealTimeId) {
		client.ensureToken();

		var res = client.callWithRetry(
			() -> client.http().get(
				Endpoints.listMeal(dateYyyymmdd, mealTimeId, id),
				Map.of("Cookie", "cafeteriaActiveId=" + id),
				new ParameterizedTypeReference<WelstoryResponse<ApiTypes.MealListData>>() {
				}
			)
		);

		return client.unwrap(res, "식단 리스트 조회").data().mealList();
	}
}
