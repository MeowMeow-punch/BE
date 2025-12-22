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

/**
 * [Diet][Controller] FoodController
 *
 * 음식 목록/검색 API 제공
 *
 * <pre>
 * Client ▶ FoodController ▶ FoodService ▶ Repository
 * </pre>
 *
 * - 응답은 ResTemplate(code/message/data) 일관 포맷
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class FoodController {
	private final FoodService foodService;

	/**
	 * [API] 음식 리스트 조회 (커서 기반)
	 *
	 * @param cursor 다음 페이지 커서
	 * @param size   페이지 크기
	 * @return FoodListResponse
	 */
	@GetMapping("/food/list")
	public ResTemplate<FoodListResponse> getFoodList(
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size
	) {
		FoodListResponse data = foodService.getFoodList(cursor, size);
		return ResTemplate.success(HttpStatus.OK, "음식 리스트 조회 성공", data);
	}

	/**
	 * [API] 음식 검색
	 *
	 * @param keyword 검색 키워드
	 * @param cursor  다음 페이지 커서
	 * @param size    페이지 크기
	 * @return FoodSearchResponse
	 */
	@GetMapping("/food")
	public ResTemplate<FoodSearchResponse> getSearchFood(
		@RequestParam(name = "keyword") String keyword,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size
	) {
		FoodSearchResponse data = foodService.search(keyword, cursor, size);
		return ResTemplate.success(HttpStatus.OK, "음식 검색 성공", data);
	}
}
