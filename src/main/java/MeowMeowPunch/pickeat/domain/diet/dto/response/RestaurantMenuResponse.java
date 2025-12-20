package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.RestaurantMenuInfo;

// 사내 식당 메뉴 조회 응답 DTO (mealType별 메뉴 맵을 루트로 직렬화)
public record RestaurantMenuResponse(
	Map<String, List<RestaurantMenuInfo>> menus
) {
	@JsonValue
	public Map<String, List<RestaurantMenuInfo>> menus() {
		return menus;
	}

	public static RestaurantMenuResponse of(Map<String, List<RestaurantMenuInfo>> menus) {
		return new RestaurantMenuResponse(menus);
	}
}
