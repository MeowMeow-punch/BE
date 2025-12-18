package MeowMeowPunch.pickeat.domain.diet.ai;

import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.global.llm.config.LlmProperties;
import MeowMeowPunch.pickeat.global.llm.core.LlmClient;
import MeowMeowPunch.pickeat.global.llm.core.LlmRequest;
import MeowMeowPunch.pickeat.global.llm.core.LlmRequestOptions;
import MeowMeowPunch.pickeat.global.llm.core.LlmResponse;
import MeowMeowPunch.pickeat.global.llm.core.LlmUseCase;
import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietAiFacade {

	private final LlmClient llmClient;
	private final LlmProperties llmProperties;

	// 홈 추천용 LLM 호출 (프롬프트/스키마 TODO)
	public String recommendHome() {
		String system = """
		TODO: 시스템 지시문(역할/출력 JSON 강제/길이 제한/금지 규칙)
		""";
		String user = """
		TODO: 유저 프롬프트(후보 6개 + 목표/제약 + 당일섭취요약)
		""";

		LlmRequest req = new LlmRequest(
			LlmUseCase.HOME_RECOMMEND,
			system,
			user,
			LlmRequestOptions.of(
				llmProperties.generation().temperature(),
				llmProperties.generation().maxOutputTokens()
			)
		);

		LlmResponse res = llmClient.generate(req);
		return res.jsonText();
	}

	/**
	 * 홈 추천 후보(최대 6개)를 받아 AI가 1~2개를 선택하도록 호출.
	 * - 실제 선택/스키마는 TODO, 현재는 상위 2개를 반환하며 AI 호출 결과를 이유로 활용.
	 */
	public HomeRecommendationResult selectHomeRecommendations(List<FoodRecommendationCandidate> candidates) {
		if (candidates == null || candidates.isEmpty()) {
			return HomeRecommendationResult.empty("추천 후보가 없습니다.");
		}

		String reason;
		boolean aiUsed = false;
		try {
			reason = recommendHome(); // TODO: 프롬프트 확정 시 candidates를 포함해 호출하도록 변경
			aiUsed = true;
		} catch (Exception e) {
			reason = "AI 호출 실패로 기본 추천을 제공합니다.";
			aiUsed = false;
		}

		List<FoodRecommendationCandidate> picks = candidates.stream()
			.limit(2) // AI 스키마 확정 전까지 상위 2개를 임시 사용
			.toList();

		return HomeRecommendationResult.of(picks, reason, aiUsed);
	}

	// 일일 피드백 카드용 LLM 호출 (프롬프트/스키마 TODO)
	public String feedbackDaily() {
		String system = "TODO";
		String user = "TODO";

		LlmRequest req = new LlmRequest(
			LlmUseCase.DAILY_FEEDBACK,
			system,
			user,
			LlmRequestOptions.of(
				llmProperties.generation().temperature(),
				llmProperties.generation().maxOutputTokens()
			)
		);

		LlmResponse res = llmClient.generate(req);
		return res.jsonText();
	}
}
