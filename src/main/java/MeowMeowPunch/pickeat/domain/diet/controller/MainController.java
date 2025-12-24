package MeowMeowPunch.pickeat.domain.diet.controller;

import MeowMeowPunch.pickeat.global.jwt.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.service.DietService;
import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import lombok.RequiredArgsConstructor;

/**
 * [Diet][Controller] MainController
 *
 * 메인 페이지(오늘 식단/추천) 조회 API 제공
 *
 * <pre>
 * Client ▶ MainController ▶ DietService ▶ Repository/Assembler
 * </pre>
 *
 * - 응답은 ResTemplate(code/message/data) 일관 포맷
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class MainController {
	private final DietService dietService;

	/**
	 * [API] 메인 페이지 조회
	 *
	 * @param principal 사용자 식별자
	 * @return DietHomeResponse
	 */
	@GetMapping("/main")
	public ResTemplate<DietHomeResponse> getDiet(
        @AuthenticationPrincipal UserPrincipal principal
	) {
        String userId = principal.getUserId().toString();
		DietHomeResponse data = dietService.getHome(userId);
		return ResTemplate.success(HttpStatus.OK, "메인페이지 조회 성공", data);
	}
}
