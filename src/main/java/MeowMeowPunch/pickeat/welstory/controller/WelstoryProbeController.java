package MeowMeowPunch.pickeat.welstory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import MeowMeowPunch.pickeat.welstory.WelstoryClient;
import MeowMeowPunch.pickeat.welstory.WelstoryRestaurant;
import MeowMeowPunch.pickeat.welstory.api.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/welstory")
public class WelstoryProbeController {

	private final WelstoryClient client;
	private final WelstoryMenuService welstoryMenuService;

	// 1) 식당 검색 가능 여부 확인
	@GetMapping("/restaurants")
	public ResTemplate<List<ApiTypes.RawRestaurantData>> searchRestaurants(
		@RequestParam(name = "query") String query
	) {
		var data = client.searchRestaurantRaw(query);
		return ResTemplate.success(HttpStatus.OK, "식당 검색 성공", data);
	}

	// 1-1) 내 식당 목록 조회 (등록된 식당 ID 확인용)
	@GetMapping("/my-restaurants")
	public ResTemplate<List<ApiTypes.RegisteredRestaurantData>> myRestaurants() {
		var data = client.listMyRestaurants();
		return ResTemplate.success(HttpStatus.OK, "내 식당 목록 조회 성공", data);
	}

	// 2) 식사 시간대 목록 조회 가능 여부 확인
	@GetMapping("/meal-times")
	public ResTemplate<List<ApiTypes.MealTimeData>> mealTimes(
		@RequestParam String restaurantId,
		@RequestParam(required = false) String restaurantName,
		@RequestParam(required = false) String restaurantDesc
	) {
		WelstoryRestaurant r = client.restaurant(
			restaurantId,
			restaurantName == null ? "" : restaurantName,
			restaurantDesc == null ? "" : restaurantDesc
		);
		var data = r.listMealTimesRaw();
		return ResTemplate.success(HttpStatus.OK, "식사 시간대 조회 성공", data);
	}

	// 3) 특정 날짜/식당/시간대 식단 리스트 조회 가능 여부 확인
	@GetMapping("/meals")
	public ResTemplate<List<WelstoryMenuItem>> meals(
		@RequestParam String restaurantId,
		@RequestParam(required = false) Integer date,              // yyyymmdd
		@RequestParam String mealTimeId,
		@RequestParam(required = false) String mealTimeName
	) {
		var data = welstoryMenuService.getMenus(restaurantId, date, mealTimeId, mealTimeName);
		return ResTemplate.success(HttpStatus.OK, "식단 리스트 조회 성공", data);
	}

	// 4) 영양 정보 조회 가능 여부 확인
	@GetMapping("/nutrients")
	public ResTemplate<List<ApiTypes.RawMealMenuData>> nutrients(
		@RequestParam String restaurantId,
		@RequestParam(required = false) Integer date,              // yyyymmdd
		@RequestParam String mealTimeId,
		@RequestParam String hallNo,
		@RequestParam String menuCourseType
	) {
		var data = welstoryMenuService.getNutrients(restaurantId, date, mealTimeId, hallNo, menuCourseType);
		return ResTemplate.success(HttpStatus.OK, "영양 정보 조회 성공", data);
	}
}
