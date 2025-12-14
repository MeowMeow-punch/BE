package MeowMeowPunch.pickeat.domain.diet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeowMeowPunch.pickeat.domain.diet.dto.response.DietResponse;
import MeowMeowPunch.pickeat.domain.diet.service.DietService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class MainController {
	private final DietService dietService;

	// 메인 페이지 조회
	@GetMapping("/main")
	public ResTemplate<DietResponse> getDiet(
		@RequestParam(name = "userId") String userId
	) {
		DietResponse data = dietService.getHome(userId);
		return ResTemplate.success(HttpStatus.OK, "메인페이지 조회 성공", data);
	}
}
