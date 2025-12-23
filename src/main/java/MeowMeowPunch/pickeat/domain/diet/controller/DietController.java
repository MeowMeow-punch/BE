package MeowMeowPunch.pickeat.domain.diet.controller;

import MeowMeowPunch.pickeat.global.jwt.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

/**
 * [Diet][Controller] DietController
 *
 * 식단 조회/등록/수정/삭제 및 사내 식당 메뉴 조회 API 제공
 *
 * <pre>
 * Client
 *   │
 *   ▼
 * [DietController] ──▶ [DietService] ──▶ Repository/Assembler
 *   │                        │
 *   ▼                        ▼
 * [ResTemplate 응답] ◀── DTO 조립
 * </pre>
 *
 * - 응답은 ResTemplate(code/message/data) 일관 포맷
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {
	private final DietService dietService;

	/**
	 * [API] 날짜별 식단 페이지 조회
	 *
	 * @param principal 사용자 식별자
	 * @param date   조회 날짜(YYYY-MM-DD)
	 * @return DailyDietResponse
	 */
	@GetMapping
	public ResTemplate<DailyDietResponse> getDiet(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam(name = "date") String date
	) {
        String userId = principal.getUserId().toString();
		DailyDietResponse data = dietService.getDaily(userId, date);
		return ResTemplate.success(HttpStatus.OK, "식단 페이지 조회 성공", data);
	}

	/**
	 * [API] 식단 상세 조회
	 *
	 * @param principal 사용자 식별자
	 * @param dietId 식단 ID
	 * @return DietDetailResponse
	 */
	@GetMapping("/{myDietId}")
	public ResTemplate<DietDetailResponse> getDietDetail(
        @AuthenticationPrincipal UserPrincipal principal,
		@PathVariable("myDietId") Long dietId
	) {
        String userId = principal.getUserId().toString();
		DietDetailResponse data = dietService.getDetail(userId, dietId);
		return ResTemplate.success(HttpStatus.OK, "식단 상세 조회 성공", data);
	}

	/**
	 * [API] 식단 상세 영양분 조회
	 *
	 * @param principal 사용자 식별자
	 * @param date   조회 날짜(YYYY-MM-DD)
	 * @return NutritionResponse
	 */
	@GetMapping("/nutrient")
	public ResTemplate<NutritionResponse> getDietNutrition(
        @AuthenticationPrincipal UserPrincipal principal,
		@RequestParam(name = "date") String date
	) {
        String userId = principal.getUserId().toString();
		NutritionResponse data = dietService.getNutrition(userId, date);
		return ResTemplate.success(HttpStatus.OK, "식단 상세 조회 성공", data);
	}

	/**
	 * [API] 사내 식당 메뉴 조회
	 *
	 * @param date 조회 날짜(YYYY-MM-DD)
	 * @return RestaurantMenuResponse
	 */
	@GetMapping("/restaurant/menu")
	public ResTemplate<RestaurantMenuResponse> getRestaurantMenu(
        @AuthenticationPrincipal UserPrincipal principal,
		@RequestParam(name = "date") String date
	) {
        String userId = principal.getUserId().toString();
		RestaurantMenuResponse data = dietService.getRestaurantMenus(date, userId);
		return ResTemplate.success(HttpStatus.OK, "사내 식당 메뉴 조회 성공", data);
	}

	/**
	 * [API] 추천 식단 등록
	 *
	 * @param principal 사용자 식별자
	 * @param recommendationId 추천 식단 ID
	 * @return DietRegisterResponse
	 */
	@PostMapping({"/recommendation/{recommendationId}"})
	public ResTemplate<DietRegisterResponse> registerRecommendation(
        @AuthenticationPrincipal UserPrincipal principal,
		@PathVariable("recommendationId") Long recommendationId
	) {
        String userId = principal.getUserId().toString();
		DietRegisterResponse data = dietService.registerRecommendation(userId, recommendationId);
		return ResTemplate.success(HttpStatus.OK, "추천 식단 등록 성공", data);
	}

	/**
	 * [API] 식단 등록
	 *
	 * @param principal  사용자 식별자
	 * @param request 식단 등록 요청 페이로드
	 * @return void
	 */
	@PostMapping
	public ResTemplate<Void> createDiet(
        @AuthenticationPrincipal UserPrincipal principal,
		@Valid @RequestBody DietRequest request
	) {
        String userId = principal.getUserId().toString();
		dietService.create(userId, request);
		return ResTemplate.success(HttpStatus.OK, "식단 등록 성공");
	}

	/**
	 * [API] 식단 수정
	 *
	 * @param principal  사용자 식별자
	 * @param myDietId 식단 ID
	 * @param request 수정 요청 페이로드
	 * @return void
	 */
	@PutMapping("/{myDietId}")
	public ResTemplate<Void> updateDiet(
        @AuthenticationPrincipal UserPrincipal principal,
		@PathVariable("myDietId") Long myDietId,
		@Valid @RequestBody DietRequest request
	) {
        String userId = principal.getUserId().toString();
		dietService.update(userId, myDietId, request);
		return ResTemplate.success(HttpStatus.OK, "식단 수정 성공");
	}

	/**
	 * [API] 식단 삭제
	 *
	 * @param principal 사용자 식별자
	 * @param myDietId 식단 ID
	 * @return void
	 */
	@DeleteMapping("/{myDietId}")
	public ResTemplate<Void> deleteDiet(
        @AuthenticationPrincipal UserPrincipal principal,
		@PathVariable("myDietId") Long myDietId
	) {
        String userId = principal.getUserId().toString();
		dietService.delete(userId, myDietId);
		return ResTemplate.success(HttpStatus.OK, "식단 삭제 성공");
	}
}
