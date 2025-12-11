package MeowMeowPunch.pickeat.domain.diet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	public ResTemplate<DietHomeResponse> getHome(
		@RequestParam(name = "userId") String userId,
		@RequestParam(name = "purpose", required = false, defaultValue = "BALANCE") String purpose
	) {
		DietHomeResponse data = dietService.getHome(userId, purpose);
		return new ResTemplate<>(HttpStatus.OK, "메인페이지 조회 성공", data);
	}
}
