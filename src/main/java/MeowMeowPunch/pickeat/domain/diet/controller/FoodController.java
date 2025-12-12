package MeowMeowPunch.pickeat.domain.diet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodListResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodSearchResponse;
import MeowMeowPunch.pickeat.domain.diet.service.FoodService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class FoodController {
	private final FoodService foodService;

	// 음식 리스트 조회
	@GetMapping("/list/search")
	public ResTemplate<FoodListResponse> getFoodList(
		// @RequestHeader(name = "Authorization", required = false) String authorization,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size
	) {
		FoodListResponse data = foodService.getFoodList(cursor, size);
		return new ResTemplate<>(HttpStatus.OK, "음식 리스트 조회 성공", data);
	}

	// 키워드 기반 음식 리스트 조회
	@GetMapping("/search")
	public ResTemplate<FoodSearchResponse> getSearchFood(
		@RequestParam(name = "keyword", required = true) String keyword,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size
	) {
		FoodSearchResponse data = foodService.search(keyword, cursor, size);
		return new ResTemplate<>(HttpStatus.OK, "음식 검색 성공", data);
	}
}
