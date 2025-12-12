package MeowMeowPunch.pickeat.domain.diet.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import MeowMeowPunch.pickeat.domain.diet.dto.request.DietCreateRequest;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DailyDietResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietResponse;
import MeowMeowPunch.pickeat.domain.diet.service.DietService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {
	private final DietService dietService;

	// 식단 메인, 날짜별 조회 (date 유무로 분기)
	@GetMapping
	public ResTemplate<DietResponse> getDiet(
		@RequestParam(name = "userId") String userId,
		@RequestParam(name = "date", required = false) String date
	) {
		DietResponse data = Optional.ofNullable(date)
			.filter(d -> !d.isBlank())
			.<DietResponse>map(d -> dietService.getDaily(userId, d))
			.orElseGet(() -> dietService.getHome(userId));

		String message = (data instanceof DailyDietResponse)
			? "식단 페이지 조회 성공"
			: "메인페이지 조회 성공";

		return ResTemplate.success(HttpStatus.OK, message, data);
	}

	// 식단 등록
	@PostMapping("/create")
	public ResTemplate<Void> createDiet(
		@RequestParam(name = "userId") String userId,
		@Valid @RequestBody DietCreateRequest request
	) {
		dietService.create(userId, request);
		return ResTemplate.success(HttpStatus.OK, "식단 등록 성공");
	}
}
