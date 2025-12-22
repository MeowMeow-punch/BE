package MeowMeowPunch.pickeat.domain.diet.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.domain.diet.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.LlmUseCase;
import MeowMeowPunch.pickeat.global.llm.config.LlmProperties;
import MeowMeowPunch.pickeat.global.llm.dto.LlmClient;
import MeowMeowPunch.pickeat.global.llm.dto.LlmRequest;
import MeowMeowPunch.pickeat.global.llm.dto.LlmRequestOptions;
import MeowMeowPunch.pickeat.global.llm.dto.LlmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Diet][AI] AI 식단 추천 및 피드백 파사드.
 *
 * <pre>
 * Client
 *   │
 *   ▼
 * [DietService] ──▶ [DietRecommendationService] ──▶ [DietAiFacade] ──▶ [LlmClient] ──▶ (OpenAI)
 * </pre>
 *
 * - 홈 화면 식단 추천 (Score + Context)
 * - 일일 영양 피드백 (Cold Start / Daily Balance)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietAiFacade {

	private final LlmClient llmClient;
	private final LlmProperties llmProperties;
	private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

	/**
	 * [Home] AI 추천 선택 및 이유 생성
	 *
	 * @param focus      사용자 영양 목표 (HEALTH, DIET, MUSCLE)
	 * @param mealSlot   식사 시간대 (BREAKFAST, LUNCH, DINNER)
	 * @param candidates 추천 후보 식단 리스트
	 * @return HomeRecommendationResult (선택된 식단 인덱스 및 이유)
	 */
	public HomeRecommendationResult recommendHome(Focus focus, DietType mealSlot,
		List<FoodRecommendationCandidate> candidates, String userId) {
		if (candidates == null || candidates.isEmpty()) {
			return HomeRecommendationResult.empty("추천 후보가 없습니다.");
		}

		if (!hasLlmConfig()) {
			log.warn("LLM 설정이 비어 있어 AI 추천을 건너뜁니다. openai={}, generation={}", llmProperties.openai(),
				llmProperties.generation());
			return fallback(candidates, "AI 설정이 없어 점수 기반으로 추천했어요.");
		}

		try {
			// 1. 프롬프트 데이터 구성
			Map<String, Object> inputJson = buildHomePromptInput(focus, mealSlot, candidates, userId);
			String userPrompt = objectMapper.writeValueAsString(inputJson);

			// 2. 시스템 지시문
			String systemPrompt = """
				당신은 전문 영양사입니다. 다음 기준을 50:50으로 고려하여 후보 중 2개를 선정하세요.
				1. Score (50%): 서버가 계산한 영양 적합도 점수가 높은 메뉴를 우선 고려합니다.
				2. Context (50%): 사용자의 질환, 목표 등 미묘한 상황을 판단합니다.
				    - Healthy: 질환(고혈압 등)에 해로운 영양소 회피
				    - Diet/Muscle: 목표 달성을 위한 칼로리/탄수화물/단백질/지방 준수
								
				Output JSON Format:
				{
				  "pickedIndices": [0, 3],
				  "reason": "한국어 한 줄 평"
				}
				""";

			// 3. LLM 호출
			LlmProperties.Generation gen = safeGeneration();
			LlmRequest req = new LlmRequest(
				LlmUseCase.HOME_RECOMMEND,
				systemPrompt,
				userPrompt,
				LlmRequestOptions.of(
					gen.temperature(),
					gen.maxOutputTokens()
				)
			);

			LlmResponse res = llmClient.generate(req);
			String jsonText = res.jsonText();

			// 4. 응답 파싱
			AiRecommendationOutput output = objectMapper.readValue(jsonText, AiRecommendationOutput.class);

			// 5. 결과 매핑
			List<FoodRecommendationCandidate> picks = output.pickedIndices().stream()
				.filter(idx -> idx >= 0 && idx < candidates.size())
				.map(candidates::get)
				.toList();

			if (picks.isEmpty()) {
				// AI가 유효하지 않은 인덱스를 반환한 경우 Fallback
				return fallback(candidates, "AI 추천 결과가 올바르지 않아 점수 기반으로 추천해 드립니다.");
			}

			return HomeRecommendationResult.of(picks, output.reason());

		} catch (Exception e) {
			log.error("AI Recommendation Failed", e);
			return fallback(candidates, "목표 영양에 근접한 메뉴를 우선 추천했어요.");
		}
	}

	/**
	 * [Daily] 일일 영양 피드백 생성
	 *
	 * @param isFirstMeal 오늘 첫 끼니 여부
	 * @param balance     오늘 영양 섭취 요약 (과잉/부족 등)
	 * @param lastRecord  (Cold Start) 최근 식사 기록
	 * @return AI가 생성한 한 줄 피드백
	 */
	public String feedbackDaily(boolean isFirstMeal, NutrientTotals balance, NutrientTotals lastRecord) {
		try {
			Map<String, Object> inputJson = new HashMap<>();
			inputJson.put("isFirstMeal", isFirstMeal);

			if (isFirstMeal && lastRecord != null) {
				// Cold Start: 지난 기록 활용
				inputJson.put("lastDietRecord", Map.of(
					"totalKcal", lastRecord.totalKcal(),
					"totalCarbs", lastRecord.totalCarbs(),
					"totalProtein", lastRecord.totalProtein(),
					"totalFat", lastRecord.totalFat()
				));
			} else if (balance != null) {
				// Today Balance Logic
				// 여기서는 NutrientTotals 자체를 넘기기보다, '가장 심각한 이슈' 하나를 넘기는게 좋음
				// 하지만 편의상 전체 객체를 넘기고 프롬프트에서 판단하게 할 수도 있음.
				// 계획대로 '서버 계산된 과잉/부족'을 넘기려면 로직 필요.
				// MVP: 일단 Balance 전체 전달
				inputJson.put("balance", balance);
			}

			String userPrompt = objectMapper.writeValueAsString(inputJson);
			String systemPrompt = """
				사용자의 식습관을 한 줄로 피드백하세요.
				- lastDietRecord가 있으면(오늘 첫 끼), 과거 기록을 참고해 오늘 보완할 점을 제안하세요.
				- balance가 있으면, 현재 영양 상태에 대해 과잉/부족을 조언하세요.
				- Output: 한국어 문장 하나.
				""";

			LlmRequest req = new LlmRequest(
				LlmUseCase.DAILY_FEEDBACK,
				systemPrompt,
				userPrompt,
				LlmRequestOptions.of(
					llmProperties.generation().temperature(),
					llmProperties.generation().maxOutputTokens()
				)
			);

			LlmResponse res = llmClient.generate(req);
			return res.jsonText().trim(); // String response

		} catch (Exception e) {
			log.error("AI Daily Feedback Failed", e);
			throw new MeowMeowPunch.pickeat.global.llm.exception.LlmException("일일 피드백 생성 실패", e);
		}
	}

	// --- Private Helpers ---
	private HomeRecommendationResult fallback(List<FoodRecommendationCandidate> candidates, String reason) {
		List<FoodRecommendationCandidate> fallbackPicks = candidates.stream()
			.limit(2)
			.toList();
		return HomeRecommendationResult.of(fallbackPicks, reason);
	}

	private boolean hasLlmConfig() {
		return llmProperties != null
			&& llmProperties.openai() != null
			&& llmProperties.openai().apiKey() != null
			&& llmProperties.openai().model() != null;
	}

	private LlmProperties.Generation safeGeneration() {
		LlmProperties.Generation gen = llmProperties.generation();
		if (gen == null) {
			return new LlmProperties.Generation(0.7, 256);
		}
		return gen;
	}

	private Map<String, Object> buildHomePromptInput(Focus focus, DietType mealSlot,
		List<FoodRecommendationCandidate> candidates, String userId) {
		Map<String, Object> json = new HashMap<>();

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(UserNotFoundException::new);

		// Context
		json.put("context", Map.of(
			"mealSlot", mealSlot.name(),
			"focus", focus.name()
		));

		// User Profile
		Map<String, Object> profile = new HashMap<>();
		if (focus == Focus.HEALTH) {
			profile.put("diseases", user.getDiseases());
			profile.put("lifestyle", Map.of("isSmoking", user.getSmokingStatus().name(), "isDrink", user.getDrinkingStatus().name()));
		} else {
			profile.put("goal", Map.of("targetWeight", user.getTargetWeight()));
			profile.put("dietPolicy", "LOW_CALORIE");
		}
		profile.put("allergies", user.getAllergies()); // Common
		json.put("userProfile", profile);

		// Candidates (Simplified with Index)
		List<Map<String, Object>> candidateList = IntStream.range(0, candidates.size())
			.mapToObj(i -> {
				FoodRecommendationCandidate c = candidates.get(i);
				return Map.of(
					"idx", i,
					"name", c.name(),
					"nutrients", Map.of(
						"kcal", c.kcal(),
						"carbs", c.carbs(),
						"fat", c.fat(),
						"protein", c.protein()
					),
					"score", c.score()
				);
			})
			.toList();
		json.put("candidates", candidateList);

		return json;
	}

	// Inner Record for Response
	private record AiRecommendationOutput(List<Integer> pickedIndices, String reason) {
	}
}
