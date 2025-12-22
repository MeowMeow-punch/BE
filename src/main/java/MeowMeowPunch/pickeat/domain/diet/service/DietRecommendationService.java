package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.UUID;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.domain.diet.exception.UserNotFoundException;
import MeowMeowPunch.pickeat.global.common.enums.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.diet.ai.DietAiFacade;
import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.entity.AiFeedBack;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDietFood;
import MeowMeowPunch.pickeat.domain.diet.exception.DietFeedbackGenerateException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietRecommendationSaveException;
import MeowMeowPunch.pickeat.domain.diet.exception.FoodNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.repository.AiFeedBackRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;
import MeowMeowPunch.pickeat.welstory.repository.GroupMappingRepository;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Diet][Service] 식단 추천 계산 서비스
 *
 * - 그룹/개인 상황에 맞는 추천 후보 조회
 * - 추천 식단 저장 및 링크/수량 처리
 * - AI 추천 호출과 사유(Reason) 저장
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DietRecommendationService {
	private static final int TOP_LIMIT = 6;
	private static final int MIN_PICK = 1;
	private static final int KCAL_TOLERANCE = 200; // +- 칼로�?기�?
	private static final String BASE_UNIT_GRAM = "G";
	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	private static final String WELSTORY_LUNCH_ID = "2";
	private static final String WELSTORY_LUNCH_NAME = "?�심";
	// TODO: ?�용?�에??가?�오�?
	private static final BigDecimal GOAL_KCAL = BigDecimal.valueOf(2000);
	private static final BigDecimal GOAL_CARBS = BigDecimal.valueOf(280);
	private static final BigDecimal GOAL_PROTEIN = BigDecimal.valueOf(120);
	private static final BigDecimal GOAL_FAT = BigDecimal.valueOf(70);

	private static final Map<DietType, BigDecimal> MEAL_RATIO = Map.of(
			DietType.BREAKFAST, new BigDecimal("0.30"),
			DietType.LUNCH, new BigDecimal("0.30"),
			DietType.DINNER, new BigDecimal("0.30"),
			DietType.SNACK, new BigDecimal("0.10"));

	private final DietRecommendationMapper dietRecommendationMapper;
	private final RecommendedDietRepository recommendedDietRepository;
	private final RecommendedDietFoodRepository recommendedDietFoodRepository;
	private final FoodRepository foodRepository;
	private final DietRepository dietRepository;
	private final GroupMappingRepository groupMappingRepository;
	private final WelstoryMenuService welstoryMenuService;
	private final DietAiFacade dietAiFacade;
	private final AiFeedBackRepository aiFeedBackRepository;
	private final UserRepository userRepository;

	/**
	 * [Recommend] ?�늘/?�재 ?�사 ?�롯??맞춰 추천 TOP5 계산 + AI ?�택
	 *
	 * @param userId ?�용???�별??
	 * @param focus  추천 목적(균형/?�백�???
	 * @param totals ?�늘 ??�� ?�계
	 * @return 추천 결과 (Picks + Reason)
	 */
	@Transactional
	public HomeRecommendationResult recommendTopFoods(String userId, Focus focus, NutrientTotals totals) {
		LocalDate today = LocalDate.now(KOREA_ZONE);
		LocalTime nowTime = LocalTime.now(KOREA_ZONE);
		DietType mealSlot = mealSlot(nowTime);

		User user = userRepository.findById(UUID.fromString(userId))
				.orElseThrow(UserNotFoundException::new);

		String groupName = DietPageAssembler.getGroupName(user, groupMappingRepository);

		// 1. ?��? ?�성??추천 조회
		List<RecommendedDiet> existing = recommendedDietRepository.findByUserIdAndDateAndDietTypeOrderByCreatedAtDesc(
				userId, today, mealSlot);

		if (existing.size() >= MIN_PICK) {
			List<FoodRecommendationCandidate> picks = existing.stream()
					.sorted(Comparator.comparing(RecommendedDiet::getScore, Comparator.nullsLast(Double::compareTo))
							.reversed())
					.map(this::toCandidate)
					.toList();

			// ?�?�된 ?�드�??�유 조회
			String reason = aiFeedBackRepository.findByUserIdAndDateAndType(userId, today, FeedBackType.RECOMMENDATION)
					.map(AiFeedBack::getContent)
					.orElse("목표 ?�양??근접??메뉴�??�선 추천?�어??");

			return HomeRecommendationResult.of(picks, reason);
		}

		List<FoodRecommendationCandidate> candidates;

		// 2. ?�스?�리(그룹) ?�심 ?�선 ?�인
		if (isGroupUser(userId) && mealSlot == DietType.LUNCH) {
			candidates = recommendWelstoryLunch(today, focus, totals, groupName);
		} else {
			// 3. ?�반 배달/?�당(Food DB) ?�보 ?�성
			candidates = recommendGeneralFoods(mealSlot, focus, totals);
		}

		// 4. AI ?�출?�여 최종 Pick & Reason ?�득
		HomeRecommendationResult aiResult = dietAiFacade.recommendHome(focus, mealSlot, candidates, userId);

		// 5. ?�??
		try {
			// ?�택??메뉴 ?�??
			saveTopRecommended(userId, today, mealSlot, aiResult.picks());

			// AI ?�유 ?�??(Daily Recommendation Feedback)
			// 기존??같�? ?�짜/?�?�의 ?�드백이 ?�다�??�데?�트
			AiFeedBack feedback = aiFeedBackRepository.findByUserIdAndDateAndType(userId, today,
					FeedBackType.RECOMMENDATION)
					.orElse(AiFeedBack.builder()
							.userId(userId)
							.date(today)
							.type(FeedBackType.RECOMMENDATION)
							.content(aiResult.reason())
							.build());

			feedback.updateContent(aiResult.reason());
			aiFeedBackRepository.save(feedback);

			return aiResult;

		} catch (Exception e) {
			throw new DietRecommendationSaveException(e);
		}
	}

	private List<FoodRecommendationCandidate> recommendGeneralFoods(DietType mealSlot, Focus focus,
			NutrientTotals totals) {
		BigDecimal targetMealKcal = targetForMeal(GOAL_KCAL, totals.totalKcal(), mealSlot);
		BigDecimal targetMealCarbs = targetMacroForMeal(GOAL_CARBS, totals.totalCarbs(), mealSlot);
		BigDecimal targetMealProtein = targetMacroForMeal(GOAL_PROTEIN, totals.totalProtein(), mealSlot);
		BigDecimal targetMealFat = targetMacroForMeal(GOAL_FAT, totals.totalFat(), mealSlot);
		List<String> allowedCategories = allowedCategoriesForMeal(mealSlot);
		Weight weight = weightByPurpose(focus);

		return dietRecommendationMapper.findTopFoodCandidates(
				targetMealKcal,
				targetMealCarbs,
				targetMealProtein,
				targetMealFat,
				allowedCategories,
				weight.kcal(),
				weight.carbs(),
				weight.protein(),
				weight.fat(),
				weight.penaltyOverKcal(),
				weight.penaltyOverMacro(),
				KCAL_TOLERANCE,
				BASE_UNIT_GRAM,
				TOP_LIMIT);
	}

	/**
	 * [Recommend] ?�스?�리 ?�심 ?�단 ?�보 조회 �??�수??
	 */
	private List<FoodRecommendationCandidate> recommendWelstoryLunch(LocalDate targetDate, Focus focus,
			NutrientTotals totals, String groupName) {

		log.info("[DietRecommendationService][Welstory] start: date={}, groupName='{}'", targetDate, groupName);
		String restaurantId = groupMappingRepository.findByGroupName(groupName)
				.map(GroupMapping::getGroupId)
				.orElse(null);
		if (restaurantId == null) {
			log.info("[DietRecommendationService][Welstory] no restaurant mapping: groupName='{}'", groupName);
			return List.of();
		}

		int dateYyyymmdd = toYyyymmdd(targetDate);
		log.info("[DietRecommendationService][Welstory] fetch candidates: restaurantId={}, dateYyyymmdd={}",
				restaurantId, dateYyyymmdd);
		List<FoodRecommendationCandidate> menus = welstoryMenuService.getRecommendationCandidates(
				restaurantId, dateYyyymmdd, WELSTORY_LUNCH_ID, WELSTORY_LUNCH_NAME);
		if (menus.isEmpty()) {
			log.info("[DietRecommendationService][Welstory] no candidates returned");
			return List.of();
		}
		log.info("[DietRecommendationService][Welstory] candidates fetched: count={}", menus.size());

		BigDecimal targetMealKcal = targetForMeal(GOAL_KCAL, totals.totalKcal(), DietType.LUNCH);
		BigDecimal targetMealCarbs = targetMacroForMeal(GOAL_CARBS, totals.totalCarbs(), DietType.LUNCH);
		BigDecimal targetMealProtein = targetMacroForMeal(GOAL_PROTEIN, totals.totalProtein(), DietType.LUNCH);
		BigDecimal targetMealFat = targetMacroForMeal(GOAL_FAT, totals.totalFat(), DietType.LUNCH);
		Weight weight = weightByPurpose(focus);

		// ?�보 ?�성 ?�계?�서???�단 ?�렬?�서 ?�겨�?(AI 참고??
		return menus.stream()
				.map(m -> scoreCandidate(m, targetMealKcal, targetMealCarbs, targetMealProtein, targetMealFat, weight))
				.sorted((a, b) -> Double.compare(b.score(), a.score()))
				.toList();
	}

	/**
	 * [Score] ?�스?�리 ?�보?� 목표 ?�양 차이�?가중치�??�수??
	 */
	private FoodRecommendationCandidate scoreCandidate(FoodRecommendationCandidate c, BigDecimal targetKcal,
			BigDecimal targetCarb, BigDecimal targetProtein, BigDecimal targetFat, Weight weight) {
		double kcalDiff = diff(c.kcal(), targetKcal) * weight.kcal();
		double carbDiff = diff(c.carbs(), targetCarb) * weight.carbs();
		double proteinDiff = diff(c.protein(), targetProtein) * weight.protein();
		double fatDiff = diff(c.fat(), targetFat) * weight.fat();

		double penalty = kcalDiff + carbDiff + proteinDiff + fatDiff;
		double score = -penalty; // 차이가 ?�을?�록 ?��? ?�수

		return new FoodRecommendationCandidate(
				c.foodId(),
				c.name(),
				c.thumbnailUrl(),
				c.kcal(),
				c.carbs(),
				c.protein(),
				c.fat(),
				c.category(),
				score,
				DietSourceType.WELSTORY);
	}

	private double diff(BigDecimal value, BigDecimal target) {
		if (value == null || target == null) {
			return Double.MAX_VALUE / 4;
		}
		return value.subtract(target).abs().doubleValue();
	}

	private boolean isGroupUser(String userId) {
		User user = userRepository.findById(UUID.fromString(userId))
				.orElseThrow(UserNotFoundException::new);

		return user.getStatus() == UserStatus.GROUP;
	}

	private int toYyyymmdd(LocalDate date) {
		return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
	}

	// RecommendedDiet ?�이블에 ?�??
	private List<RecommendedDiet> saveTopRecommended(String userId, LocalDate date, DietType dietType,
			List<FoodRecommendationCandidate> picks) {
		return picks.stream().map(c -> {
			DietSourceType sourceType = c.sourceType() == null ? DietSourceType.FOOD_DB : c.sourceType();
			Long foodId = sourceType == DietSourceType.WELSTORY ? null : resolveFoodId(c);
			RecommendedDiet saved = recommendedDietRepository.save(
					RecommendedDiet.builder()
							.userId(userId)
							.foodId(foodId)
							.dietType(dietType)
							.sourceType(sourceType)
							.date(date)
							.time(LocalTime.now(KOREA_ZONE))
							.title(c.name())
							.kcal(nullSafe(c.kcal()))
							.carbs(nullSafe(c.carbs()))
							.protein(nullSafe(c.protein()))
							.fat(nullSafe(c.fat()))
							.thumbnailUrl(c.thumbnailUrl())
							.score(c.score())
							.build()

			);
			if (foodId != null) {
				recommendedDietFoodRepository.save(
						RecommendedDietFood.builder()
								.recommendedDiet(saved)
								.foodId(foodId)
								.quantity(2)
								.build());
			}
			return saved;
		}).toList();
	}

	// foodId 반환
	private Long resolveFoodId(FoodRecommendationCandidate c) {
		if (c.foodId() != null && c.foodId() > 0) {
			return c.foodId();
		}
		Food existing = foodRepository.findByName(c.name());
		if (existing != null) {
			return existing.getId();
		}
		throw new FoodNotFoundException(c.foodId() != null ? c.foodId() : -1L);
	}

	// ?????��??�출 (?�수 방�?)
	private BigDecimal remainingDaily(BigDecimal goal, BigDecimal eaten) {
		BigDecimal r = goal.subtract(nullSafe(eaten));
		return r.max(BigDecimal.ZERO);
	}

	// 칼로�??��?�?측정
	private BigDecimal targetForMeal(BigDecimal dailyGoal, BigDecimal eatenSoFar, DietType mealSlot) {
		BigDecimal remaining = remainingDaily(dailyGoal, eatenSoFar); // ?�늘 ?��? ?�여 칼로�?
		BigDecimal slotGoal = dailyGoal.multiply(
				MEAL_RATIO.getOrDefault(mealSlot, new BigDecimal("0.25"))); // ?�당 ?�니???�상??목표??
		BigDecimal rawTarget = slotGoal.min(remaining); // ?�번 ?�니가 slotGoal �??��? ?�여?�을 ?�을 ???�도�??�기?�해 ??�????��?
														// 값을 ?�한??
		BigDecimal min = new BigDecimal("250");
		BigDecimal max = new BigDecimal("800");
		return rawTarget.max(min).min(max); // ?�한, ?�한??고정
	}

	// ?�양�??��?�?측정
	private BigDecimal targetMacroForMeal(BigDecimal dailyGoalMacro, BigDecimal eatenMacro, DietType mealSlot) {
		BigDecimal remaining = remainingDaily(dailyGoalMacro, eatenMacro);
		BigDecimal slotGoal = dailyGoalMacro.multiply(MEAL_RATIO.getOrDefault(mealSlot, new BigDecimal("0.25")));
		BigDecimal rawTarget = slotGoal.min(remaining);
		return rawTarget.max(BigDecimal.ZERO);
	}

	// mealSlot 별로 ?�용??카테고리
	private List<String> allowedCategoriesForMeal(DietType mealSlot) {
		if (mealSlot == DietType.SNACK) {
			return SnackCategory.labels();
		}
		return MainMealCategory.labels();
	}

	// TODO: 가중치 값�? GPT 추천?�로 ?�의�?지?�했�?추후 개선???�정
	private Weight weightByPurpose(Focus focus) {
		return switch (focus) {
			case DIET -> new Weight(
					1.5, 1.0, 0.9, 0.8, // kcal, carbs, protein, fat 가중치
					200, // kcal 초과 ?�널??
					50 // ?�단지 초과 ?�널??
				);
			case MUSCLE -> new Weight(
					1.0, 0.9, 1.5, 0.9,
					80,
					40);
			case HEALTHY -> new Weight(
					1.0, 1.0, 1.0, 1.0,
					100,
					50);
		};
	}

	// DB???�?�된 추천???�답???�보�?변??
	private FoodRecommendationCandidate toCandidate(RecommendedDiet r) {
		DietSourceType source = r.getSourceType() != null ? r.getSourceType() : DietSourceType.FOOD_DB;
		return new FoodRecommendationCandidate(
				r.getId(), // dietId�?candidate??id ?�롯?�로 ?�달??DietService?�서 ?�용
				r.getTitle(),
				r.getThumbnailUrl(),
				nullSafe(r.getKcal()),
				nullSafe(r.getCarbs()),
				nullSafe(r.getProtein()),
				nullSafe(r.getFat()),
				null,
				r.getScore() == null ? 0.0 : r.getScore(),
				source);
	}

	/**
	 * [Event] ?�단 변�???AI ?�드�?비동�??�성 �??�??
	 */
	@Async
	@Transactional
	public void generateDailyFeedback(String userId, LocalDate date) {
		try {
			// 1. ?�늘 ??��???�인
			NutrientTotals todayTotals = dietRecommendationMapper.findTotalsByDate(userId, date);
			boolean isFirstMeal = (todayTotals == null ||
					todayTotals.totalKcal() == null ||
					todayTotals.totalKcal().compareTo(BigDecimal.ZERO) == 0);

			NutrientTotals lastRecord = null;
			if (isFirstMeal) {
				// 2. Cold Start: 최근 기록 조회
				lastRecord = dietRepository.findTopByUserIdAndDateLessThanOrderByDateDesc(userId, date)
						.map(d -> dietRecommendationMapper.findTotalsByDate(userId, d.getDate()))
						.orElse(null);
			}

			// 3. AI ?�출
			String feedback = dietAiFacade.feedbackDaily(isFirstMeal, todayTotals, lastRecord);

			// 4. ?�??(기존 ?�드�??�으�??�데?�트)
			AiFeedBack aiFeedBack = aiFeedBackRepository.findByUserIdAndDateAndType(userId, date, FeedBackType.DAILY)
					.orElse(AiFeedBack.builder()
							.userId(userId)
							.date(date)
							.type(FeedBackType.DAILY)
							.content(feedback)
							.build());

			aiFeedBack.updateContent(feedback);
			aiFeedBackRepository.save(aiFeedBack);

		} catch (Exception e) {
			throw new DietFeedbackGenerateException("?�일 ?�드�??�성 ?�패", e);
		}
	}

	// 가중치 dto
	private record Weight(
			double kcal,
			double carbs,
			double protein,
			double fat,
			double penaltyOverKcal,
			double penaltyOverMacro) {
	}

}
