package MeowMeowPunch.pickeat.domain.diet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.diet.dto.response.DailyDietResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.service.DietService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {
	private final DietService dietService;

	// 식단 메인 페이지
	@GetMapping
	public ResTemplate<?> getDiet(
		@RequestParam(name = "userId") String userId,
		@RequestParam(name = "date", required = false) String date
	) {
		if (StringUtils.hasText(date)) {
			DailyDietResponse data = dietService.getDaily(userId, date);
			return new ResTemplate<>(HttpStatus.OK, "식단 페이지 조회 성공", data);
		}

		DietHomeResponse data = dietService.getHome(userId);
		return new ResTemplate<>(HttpStatus.OK, "메인페이지 조회 성공", data);
	}
}
