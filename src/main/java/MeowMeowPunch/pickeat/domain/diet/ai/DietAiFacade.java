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

import MeowMeowPunch.pickeat.domain.diet.dto.DailyFeedbackPrompt;
import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionSummary;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.LlmUseCase;
import MeowMeowPunch.pickeat.global.llm.config.LlmProperties;
import MeowMeowPunch.pickeat.global.llm.dto.LlmClient;
import MeowMeowPunch.pickeat.global.llm.dto.LlmRequest;
import MeowMeowPunch.pickeat.global.llm.dto.LlmRequestOptions;
import MeowMeowPunch.pickeat.global.llm.dto.LlmResponse;
import MeowMeowPunch.pickeat.global.llm.exception.LlmException;
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

	private static final String DAILY_FEEDBACK_SYSTEM_PROMPT = """
		당신은 임상 영양사 역할을 수행합니다.
		서버가 이미 계산한 영양 상태 요약을 바탕으로
		사용자에게 안전하고 현실적인 한 줄 식단 피드백을 작성하세요.

		[중요 규칙]
		1. 과잉(EXCESS) 영양소가 하나라도 있으면 반드시 최우선으로 언급합니다.
		2. 과잉 상태가 있는 경우, 부족(DEFICIT) 영양소의 "추가 섭취"를 직접적으로 권장하지 않습니다.
		3. 부족 영양소는
		   - 과잉 문제가 없을 때만 적극 권장하거나
		   - "다음 식사에서 보완"과 같은 간접 표현으로 언급합니다.
		4. 피드백은 반드시 한국어 한 문장으로 작성합니다.
		5. 의학적 판단이나 단정적인 표현은 사용하지 않습니다.
		6. 조언은 ‘오늘’ 또는 ‘다음 식사’ 단위로 제한합니다.

		[출력 형식]
		- 일반 텍스트 한 문장 (JSON 사용 금지)
		""";
	private static final List<String> DESSERT_CATEGORIES = List.of("빵 및 과자", "유제품류 및 빙과", "음료 및 차", "과일",
		"죽 및 스프");
	private static final boolean DESSERT_BLOCKED_WHEN_NOT_SNACK = true;
	private static final double WEIGHT_KCAL = 0.3;

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
			return fallback(candidates, "AI 설정이 없어 점수 기반으로 추천했어요.", mealSlot);
		}

		try {
			// 1. 프롬프트 데이터 구성
			Map<String, Object> inputJson = buildHomePromptInput(focus, mealSlot, candidates, userId);
			String userPrompt = objectMapper.writeValueAsString(inputJson);

			// 2. 시스템 지시문
			String systemPrompt = """
				당신은 전문 영양사입니다. 서버가 제공한 후보 중 1~2개를 선택하고 한국어 한 줄 이유를 작성하세요.

				[선택 기준]
				- guidelineText를 최우선으로 준수하고, score는 보조 지표로 사용합니다(대략 70:30).
				- candidate.category를 참고해 식사/간식 적절성을 판단합니다.
				- mealSlot이 SNACK이 아니면 policy.dessertCategories에 해당하는 디저트 단독 추천을 금지합니다.

				[출력 형식(JSON)]
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
				return fallback(candidates, "AI 추천 결과가 올바르지 않아 점수 기반으로 추천해 드립니다.", mealSlot);
			}

			return HomeRecommendationResult.of(picks, output.reason(), mealSlot.name());

		} catch (Exception e) {
			log.error("AI Recommendation Failed", e);
			return fallback(candidates, "목표 영양에 근접한 메뉴를 우선 추천했어요.", mealSlot);
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
	public String feedbackDaily(boolean isFirstMeal, DietType mealSlot, Focus focus,
		NutritionSummary nutritionSummary) {
		try {
			DailyFeedbackPrompt prompt = new DailyFeedbackPrompt(
				isFirstMeal,
				mealSlot,
				focus,
				nutritionSummary == null ? NutritionSummary.empty() : nutritionSummary
			);

			String userPrompt = objectMapper.writeValueAsString(prompt);
			LlmProperties.Generation gen = safeGeneration();

			LlmRequest req = new LlmRequest(
				LlmUseCase.DAILY_FEEDBACK,
				DAILY_FEEDBACK_SYSTEM_PROMPT,
				userPrompt,
				LlmRequestOptions.of(
					gen.temperature(),
					gen.maxOutputTokens()
				)
			);

			LlmResponse res = llmClient.generate(req);
			String jsonText = res.jsonText() == null ? "" : res.jsonText().trim();
			if (jsonText.isEmpty()) {
				return res.rawText() == null ? "" : res.rawText().trim();
			}
			return jsonText;

		} catch (Exception e) {
			log.error("AI Daily Feedback Failed", e);
			throw new LlmException("일일 피드백 생성 실패", e);
		}
	}

	// --- Private Helpers ---
	private HomeRecommendationResult fallback(List<FoodRecommendationCandidate> candidates, String reason,
		DietType mealSlot) {
		List<FoodRecommendationCandidate> fallbackPicks = candidates.stream()
			.limit(2)
			.toList();
		return HomeRecommendationResult.of(fallbackPicks, reason, mealSlot.name());
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
		Map<String, Object> context = new HashMap<>();
		context.put("mealSlot", mealSlot.name());
		context.put("focus", focus.name());
		context.put("policy", Map.of(
			"dessertCategories", DESSERT_CATEGORIES,
			"dessertBlockedWhenNotSnack", DESSERT_BLOCKED_WHEN_NOT_SNACK,
			"weightKcal", WEIGHT_KCAL
		));
		json.put("context", context);

		// User Profile
		Map<String, Object> profile = new HashMap<>();
		if (focus == Focus.HEALTHY) {
			profile.put("diseases", user.getDiseases());
			profile.put("lifestyle", Map.of("isSmoking", user.getSmokingStatus().name(), "isDrink", user.getDrinkingStatus().name()));
		} else {
			profile.put("goal", Map.of("targetWeight", user.getTargetWeight()));
			profile.put("dietPolicy", "LOW_CALORIE");
		}
		profile.put("allergies", user.getAllergies()); // Common
		json.put("userProfile", profile);
		json.put("guidelineText", List.of());

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
						"protein", c.protein(),
						"fat", c.fat()
					),
					"category", c.category(),
					"sourceType", c.sourceType(),
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
