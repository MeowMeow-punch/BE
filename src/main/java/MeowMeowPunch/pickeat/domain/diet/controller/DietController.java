package MeowMeowPunch.pickeat.domain.diet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeowMeowPunch.pickeat.domain.diet.dto.request.DietRequest;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DailyDietResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietDetailResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietRegisterResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.NutritionResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.RestaurantMenuResponse;
import MeowMeowPunch.pickeat.domain.diet.service.DietService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {
	private final DietService dietService;

	// 날짜별 식단 페이지 조회
	@GetMapping
	public ResTemplate<DailyDietResponse> getDiet(
		@RequestParam(name = "userId") String userId,
		@RequestParam(name = "date") String date
	) {
		DailyDietResponse data = dietService.getDaily(userId, date);
		return ResTemplate.success(HttpStatus.OK, "식단 페이지 조회 성공", data);
	}

	// 식단 상세 조회
	@GetMapping("/{myDietId}")
	public ResTemplate<DietDetailResponse> getDietDetail(
		@RequestParam(name = "userId") String userId,
		@PathVariable("myDietId") Long dietId
	) {
		DietDetailResponse data = dietService.getDetail(userId, dietId);
		return ResTemplate.success(HttpStatus.OK, "식단 상세 조회 성공", data);
	}

	// 식단 상세 영양분 조회
	@GetMapping("/nutrient")
	public ResTemplate<NutritionResponse> getDietNutrition(
		@RequestParam(name = "userId") String userId,
		@RequestParam(name = "date") String date
	) {
		NutritionResponse data = dietService.getNutrition(userId, date);
		return ResTemplate.success(HttpStatus.OK, "식단 상세 조회 성공", data);
	}

	// 사내 식당 메뉴 조회
	@GetMapping("/restaurant/menu")
	public ResTemplate<RestaurantMenuResponse> getRestaurantMenu(
		@RequestParam(name = "date", required = false) String date
	) {
		RestaurantMenuResponse data = dietService.getRestaurantMenus(date);
		return ResTemplate.success(HttpStatus.OK, "사내 식당 메뉴 조회 성공", data);
	}

	// 추천 식단 등록
	@PostMapping({"/recommendation/{recommendationId}"})
	public ResTemplate<DietRegisterResponse> registerRecommendation(
		@RequestParam(name = "userId") String userId,
		@PathVariable("recommendationId") Long recommendationId
	) {
		DietRegisterResponse data = dietService.registerRecommendation(userId, recommendationId);
		return ResTemplate.success(HttpStatus.OK, "추천 식단 등록 성공", data);
	}

	// 식단 등록
	@PostMapping
	public ResTemplate<Void> createDiet(
		@RequestParam(name = "userId") String userId,
		@Valid @RequestBody DietRequest request
	) {
		dietService.create(userId, request);
		return ResTemplate.success(HttpStatus.OK, "식단 등록 성공");
	}

	// 식단 수정
	@PutMapping("/{myDietId}")
	public ResTemplate<Void> updateDiet(
		@RequestParam(name = "userId") String userId,
		@PathVariable("myDietId") Long myDietId,
		@Valid @RequestBody DietRequest request
	) {
		dietService.update(userId, myDietId, request);
		return ResTemplate.success(HttpStatus.OK, "식단 수정 성공");
	}

	// 식단 삭제
	@DeleteMapping("/{myDietId}")
	public ResTemplate<Void> deleteDiet(
		@RequestParam(name = "userId") String userId,
		@PathVariable("myDietId") Long myDietId
	) {
		dietService.delete(userId, myDietId);
		return ResTemplate.success(HttpStatus.OK, "식단 삭제 성공");
	}
}
