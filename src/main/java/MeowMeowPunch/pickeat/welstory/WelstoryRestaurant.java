package MeowMeowPunch.pickeat.welstory;

import java.util.List;
import java.util.Map;

import MeowMeowPunch.pickeat.welstory.exception.WelstoryApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;

import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryResponse;

// 식당 단위로 식사 시간대/식단 목록을 조회하는 Welstory 래퍼
@Slf4j
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
        for (int attempt = 0; attempt < 2; attempt++) {
            var res = client.callWithRetry(
                    () -> client.http().get(
                            Endpoints.listMeal(dateYyyymmdd, mealTimeId, id),
                            Map.of("Cookie", "cafeteriaActiveId=" + id),
                            new ParameterizedTypeReference<WelstoryResponse<ApiTypes.MealListData>>() {}
                    )
            );

            try {
                var body = client.unwrap(res, "식단 리스트 조회");
                var data = body.data();
                if (data.mealList() == null) {
                    log.info("[Welstory][Restaurant] data null (attempt={}): restaurantId={}, date={}, timeId={}",
                            attempt + 1, id, dateYyyymmdd, mealTimeId);
                    if (attempt == 0) {
                        // 강제 재로그인 후 재시도
                        client.http().setAccessToken(null);
                        client.ensureToken();
                        continue;
                    }
                    return List.of(); // 최종 폴백
                }
                return data.mealList();

            } catch (WelstoryApiException e) {
                String msg = e.getMessage();
                if (msg != null && (msg.contains("data가 null") || (msg.contains("HTTP 실패") && msg.contains("body=null")))) {
                    log.info("[Welstory][Restaurant] unwrap issue (data-null/body-null) -> force relogin (attempt={})", attempt + 1);
                    if (attempt == 0) {
                        client.http().setAccessToken(null);
                        client.ensureToken();
                        continue;
                    }
                    return List.of();
                }
                throw e; // 다른 예외는 기존 흐름 유지
            }
        }

        return List.of(); // 방어적 반환
    }
}
