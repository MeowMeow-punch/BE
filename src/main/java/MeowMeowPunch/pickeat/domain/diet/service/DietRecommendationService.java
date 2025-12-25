package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.domain.diet.ai.DietAiFacade;
import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionGoals;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionSummary;
import MeowMeowPunch.pickeat.domain.diet.entity.AiFeedBack;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDietFood;
import MeowMeowPunch.pickeat.domain.diet.exception.DietFeedbackGenerateException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietRecommendationSaveException;
import MeowMeowPunch.pickeat.domain.diet.exception.UserNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.repository.AiFeedBackRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.FeedBackType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.MainMealCategory;
import MeowMeowPunch.pickeat.global.common.enums.SnackCategory;
import MeowMeowPunch.pickeat.global.common.enums.UserStatus;
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
	private static final int ACTIVE_PICK_LIMIT = 2;
	private static final int CANDIDATE_POOL_LIMIT = 6;
	private static final int KCAL_TOLERANCE = 200; // +- 칼로리 허용 오차
	private static final int QUANTITY = 2; //
	private static final String BASE_UNIT_GRAM = "G";
	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	private static final String WELSTORY_LUNCH_ID = "2";
	private static final String WELSTORY_LUNCH_NAME = "중식";
	// TODO: 사용자 정보에서 가져오게 수정 예정
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
	private final NutritionGoalCalculator nutritionGoalCalculator;
	private final NutritionStatusClassifier nutritionStatusClassifier;

	/**
	 * [Recommend] 오늘/현재 식사 슬롯에 맞춰 추천 TOP5 계산 + AI 선택
	 *
	 * @param userId 사용자 식별자
	 * @param focus  추천 목적(균형/단백질 등)
	 * @param totals 오늘 섭취 합계
	 * @return 추천 결과 (Picks + Reason)
	 */
	@Transactional
	public HomeRecommendationResult recommendTopFoods(String userId, Focus focus, NutrientTotals totals) {
		DietType mealSlot = mealSlot(LocalTime.now(KOREA_ZONE));
		return recommendTopFoods(userId, focus, totals, mealSlot, false, false, false);
	}

	public HomeRecommendationResult recommendTopFoods(String userId, Focus focus, NutrientTotals totals,
		DietType mealSlot, boolean forceNew) {
		return recommendTopFoods(userId, focus, totals, mealSlot, forceNew, false, false);
	}

	/**
	 * [Recommend] 주어진 슬롯에 대해 추천 TOP5 계산 + AI 선택
	 *
	 * @param userId 사용자 식별자
	 * @param focus  추천 목적(균형/단백질 등)
	 * @param totals 오늘 섭취 합계
	 * @param mealSlot 대상 식사 슬롯
	 * @param forceNew 기존 추천 재사용 없이 항상 새로 생성할지 여부
	 * @param excludeExisting 오늘 동일 슬롯의 기존 추천 메뉴를 후보에서 제외할지 여부
	 * @param refresh 기존 풀에서 남은 후보를 소진하여 새 추천을 노출할지 여부(false면 현재 활성 추천 유지)
	 * @return 추천 결과 (Picks + Reason)
	 */
	@Transactional
	public HomeRecommendationResult recommendTopFoods(String userId, Focus focus, NutrientTotals totals,
		DietType mealSlot, boolean forceNew, boolean excludeExisting, boolean refresh) {
		LocalDate today = LocalDate.now(KOREA_ZONE);
		final double USED_SENTINEL = -1.0;

		User user = userRepository.findById(UUID.fromString(userId))
			.orElseThrow(UserNotFoundException::new);

		String groupName = DietPageAssembler.getGroupName(user, groupMappingRepository);
		List<RecommendedDiet> existingPool = recommendedDietRepository
			.findByUserIdAndDateAndDietTypeOrderByScoreAsc(userId, today, mealSlot);

		// 기존 풀 재사용 (forceNew가 아니고, 풀 존재)
		if (!forceNew && !existingPool.isEmpty()) {
			return pickFromPool(userId, today, mealSlot, existingPool, refresh, USED_SENTINEL);
		}

		List<FoodRecommendationCandidate> candidates;
		// 2. 웰스토리(그룹) 점심 우선 확인
		if (isGroupUser(user) && mealSlot == DietType.LUNCH) {
			candidates = recommendWelstoryLunch(today, focus, totals, groupName);
		} else {
			// 3. 일반 배달/식당(Food DB) 후보 생성
			NutritionGoals goals = nutritionGoalCalculator.calculateGoals(user);
			candidates = recommendGeneralFoods(mealSlot, focus, totals, goals, userId);
		}

		if (excludeExisting) {
			candidates = filterBlockedToday(userId, today, candidates);
		}

		if (candidates.size() < CANDIDATE_POOL_LIMIT) {
			log.warn("[DietRecommendationService] candidates less than pool size after filtering: userId={}, slot={}, size={}",
				userId, mealSlot, candidates.size());
		}

		// 후보 중 앞에서 6개만 풀로 사용
		List<FoodRecommendationCandidate> pool = candidates.stream()
			.limit(CANDIDATE_POOL_LIMIT)
			.toList();

		// 4. AI 호출하여 피드백 획득 (풀을 기준으로)
		HomeRecommendationResult aiResult = dietAiFacade.recommendHome(focus, mealSlot, pool, userId);
		String nonNullFeedback = (aiResult.aiFeedBack() == null || aiResult.aiFeedBack().isBlank())
			? "목표 영양에 근접한 메뉴를 엄선 추천했어요."
			: aiResult.aiFeedBack();

		// 5. 저장
		try {
			List<RecommendedDiet> savedPool = saveCandidatePool(userId, today, mealSlot, pool, USED_SENTINEL);
			List<RecommendedDiet> initialPicks = savedPool.stream()
				.filter(r -> r.getScore() != null && r.getScore() == USED_SENTINEL)
				.sorted(Comparator.comparing(RecommendedDiet::getScore))
				.limit(ACTIVE_PICK_LIMIT)
				.toList();
			List<FoodRecommendationCandidate> savedPicks = initialPicks.stream()
				.map(this::toCandidate)
				.filter(c -> mealSlot == DietType.LUNCH || c.sourceType() != DietSourceType.WELSTORY)
				.limit(ACTIVE_PICK_LIMIT)
				.toList();

			// AI 사유 저장(Daily Recommendation Feedback)
			// 기존에 같은 날짜/타입의 피드백이 있다면 업데이트
			saveFeedback(userId, today, nonNullFeedback);

			return HomeRecommendationResult.of(savedPicks, nonNullFeedback, mealSlot.name());

		} catch (Exception e) {
			throw new DietRecommendationSaveException(e);
		}
	}

	private List<FoodRecommendationCandidate> recommendGeneralFoods(DietType mealSlot, Focus focus,
		NutrientTotals totals, NutritionGoals goals, String userId) {

		BigDecimal targetMealKcal = targetForMeal(goals.kcal(), totals.totalKcal(), mealSlot);
		BigDecimal targetMealCarbs = targetMacroForMeal(goals.carbs(), totals.totalCarbs(), mealSlot);
		BigDecimal targetMealProtein = targetMacroForMeal(goals.protein(), totals.totalProtein(), mealSlot);
		BigDecimal targetMealFat = targetMacroForMeal(goals.fat(), totals.totalFat(), mealSlot);
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
			CANDIDATE_POOL_LIMIT,
			QUANTITY
		);
	}

	/**
	 * [Recommend] 웰스토리 점심 식단 후보 조회 (점수순)
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

		// 후보 생성 단계에서는 식단 정렬해서 넘김(AI 참고용)
		return menus.stream()
			.map(m -> scoreCandidate(m, targetMealKcal, targetMealCarbs, targetMealProtein, targetMealFat, weight))
			.sorted((a, b) -> Double.compare(b.score(), a.score()))
			.limit(CANDIDATE_POOL_LIMIT)
			.toList();
	}

	/**
	 * [Score] 웰스토리 후보와 목표 영양 차이에 가중치를 둔 점수
	 */
	private FoodRecommendationCandidate scoreCandidate(FoodRecommendationCandidate c, BigDecimal targetKcal,
		BigDecimal targetCarb, BigDecimal targetProtein, BigDecimal targetFat, Weight weight) {
		double kcalDiff = diff(c.kcal(), targetKcal) * weight.kcal();
		double carbDiff = diff(c.carbs(), targetCarb) * weight.carbs();
		double proteinDiff = diff(c.protein(), targetProtein) * weight.protein();
		double fatDiff = diff(c.fat(), targetFat) * weight.fat();

		double penalty = kcalDiff + carbDiff + proteinDiff + fatDiff;
		double score = -penalty; // 차이가 적을수록 높은 점수

		return new FoodRecommendationCandidate(
			c.recommendationId(),
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

	private boolean isGroupUser(User user) {
		return user.getStatus() == UserStatus.GROUP;
	}

	private int toYyyymmdd(LocalDate date) {
		return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
	}

	private HomeRecommendationResult pickFromPool(String userId, LocalDate today, DietType mealSlot,
		List<RecommendedDiet> pool, boolean refresh, double usedSentinel) {
		List<RecommendedDiet> active = pool.stream()
			.filter(r -> r.getScore() != null && r.getScore() == usedSentinel)
			.sorted(Comparator.comparing(RecommendedDiet::getScore, Comparator.nullsLast(Double::compareTo)))
			.limit(ACTIVE_PICK_LIMIT)
			.toList();

		if (!refresh && !active.isEmpty()) {
			return toResult(userId, today, mealSlot, active);
		}

		List<RecommendedDiet> remaining = pool.stream()
			.filter(r -> r.getScore() == null || r.getScore() >= 0)
			.sorted(Comparator.comparing(RecommendedDiet::getScore, Comparator.nullsLast(Double::compareTo)))
			.limit(ACTIVE_PICK_LIMIT)
			.toList();

		if (remaining.isEmpty()) {
			return HomeRecommendationResult.empty("더 이상 추천할 식단이 없습니다.");
		}

		remaining.forEach(r -> r.updateScore(usedSentinel));
		recommendedDietRepository.saveAll(remaining);

		return toResult(userId, today, mealSlot, remaining);
	}

	private HomeRecommendationResult toResult(String userId, LocalDate today, DietType mealSlot,
		List<RecommendedDiet> picksEntity) {
		String reason = aiFeedBackRepository.findByUserIdAndDateAndType(userId, today, FeedBackType.RECOMMENDATION)
			.map(AiFeedBack::getContent)
			.filter(r -> r != null && !r.isBlank())
			.orElse("목표 영양에 근접한 메뉴를 엄선 추천했어요.");

		List<FoodRecommendationCandidate> picks = picksEntity.stream()
			.map(this::toCandidate)
			.filter(c -> mealSlot == DietType.LUNCH || c.sourceType() != DietSourceType.WELSTORY)
			.limit(ACTIVE_PICK_LIMIT)
			.toList();

		return HomeRecommendationResult.of(picks, reason, mealSlot.name());
	}

	private List<FoodRecommendationCandidate> filterBlockedToday(String userId, LocalDate today,
		List<FoodRecommendationCandidate> candidates) {
		var blocked = new LinkedHashSet<String>();
		dietRepository.findAllByUserIdAndDateOrderByTimeAsc(userId, today).forEach(d ->
			addName(blocked, d.getTitle()));
		recommendedDietRepository.findByUserIdAndDateOrderByCreatedAtDesc(userId, today).forEach(r ->
			addName(blocked, r.getTitle()));

		Set<String> selected = new LinkedHashSet<>();
		return candidates.stream()
			.filter(c -> {
				String nm = normalizeName(c.name());
				if (nm.isEmpty() || blocked.contains(nm)) {
					return false;
				}
				if (selected.contains(nm)) {
					return false;
				}
				selected.add(nm);
				return true;
			})
			.toList();
	}

	private void addName(Set<String> set, String raw) {
		String nm = normalizeName(raw);
		if (!nm.isEmpty()) {
			set.add(nm);
		}
	}

	private String normalizeName(String raw) {
		return raw == null ? "" : raw.trim().toLowerCase();
	}

	private void saveFeedback(String userId, LocalDate today, String content) {
		AiFeedBack feedback = aiFeedBackRepository.findByUserIdAndDateAndType(userId, today,
				FeedBackType.RECOMMENDATION)
			.orElse(AiFeedBack.builder()
				.userId(userId)
				.date(today)
				.type(FeedBackType.RECOMMENDATION)
				.content(content)
				.build());

		feedback.updateContent(content);
		aiFeedBackRepository.save(feedback);
	}

	// RecommendedDiet 테이블에 저장
	private List<RecommendedDiet> saveCandidatePool(String userId, LocalDate date, DietType dietType,
		List<FoodRecommendationCandidate> pool, double usedSentinel) {
		final java.util.concurrent.atomic.AtomicInteger order = new java.util.concurrent.atomic.AtomicInteger(0);
		List<RecommendedDiet> saved = pool.stream().map(c -> {
			DietSourceType sourceType = c.sourceType() == null ? DietSourceType.FOOD_DB : c.sourceType();
			Long foodId = sourceType == DietSourceType.WELSTORY ? null : resolveFoodIdStrict(c, true);
			int currentOrder = order.getAndIncrement();
			boolean useNow = currentOrder < ACTIVE_PICK_LIMIT;

			RecommendedDiet savedDiet = recommendedDietRepository.save(
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
					.score(useNow ? usedSentinel : (double)currentOrder)
					.build()

			);
			if (foodId != null) {
				recommendedDietFoodRepository.save(
					RecommendedDietFood.builder()
						.recommendedDiet(savedDiet)
						.foodId(foodId)
						.quantity(2)
						.build());
			}
			return savedDiet;
		}).toList();
		return saved;
	}

	// foodId 반환 (저장용 - 없으면 예외/또는 null 허용)
	private Long resolveFoodIdStrict(FoodRecommendationCandidate c, boolean allowMissing) {
		if (c.sourceType() == DietSourceType.WELSTORY) {
			return null;
		}
		if (c.foodId() != null && c.foodId() > 0) {
			return c.foodId();
		}
		if (c.recommendationId() != null && c.recommendationId() > 0) {
			return c.recommendationId();
		}
		Food existing = foodRepository.findByName(c.name());
		if (existing == null) {
			if (allowMissing) {
				return null;
			}
			return null;
		}
		return existing.getId();
	}

	// 남은 양 산출 (음수 방지)
	private BigDecimal remainingDaily(BigDecimal goal, BigDecimal eaten) {
		BigDecimal r = goal.subtract(nullSafe(eaten));
		return r.max(BigDecimal.ZERO);
	}

	// 칼로리 타겟 범위 설정
	private BigDecimal targetForMeal(BigDecimal dailyGoal, BigDecimal eatenSoFar, DietType mealSlot) {
		BigDecimal remaining = remainingDaily(dailyGoal, eatenSoFar); // 오늘 남은 여유 칼로리
		BigDecimal slotGoal = dailyGoal.multiply(
			MEAL_RATIO.getOrDefault(mealSlot, new BigDecimal("0.25"))); // 해당 끼니 통상적 목표값
		BigDecimal rawTarget = slotGoal.min(remaining); // 이번 끼니가 slotGoal 정돈데 남은게 적을 수 있으므로 둘 중 작은 값을 목표로
		// 값을 제한함
		BigDecimal min = new BigDecimal("250");
		BigDecimal max = new BigDecimal("800");
		return rawTarget.max(min).min(max); // 하한, 상한 고정
	}

	// 영양소 타겟 범위 설정
	private BigDecimal targetMacroForMeal(BigDecimal dailyGoalMacro, BigDecimal eatenMacro, DietType mealSlot) {
		BigDecimal remaining = remainingDaily(dailyGoalMacro, eatenMacro);
		BigDecimal slotGoal = dailyGoalMacro.multiply(MEAL_RATIO.getOrDefault(mealSlot, new BigDecimal("0.25")));
		BigDecimal rawTarget = slotGoal.min(remaining);
		return rawTarget.max(BigDecimal.ZERO);
	}

	// mealSlot 별로 허용할 카테고리
	private List<String> allowedCategoriesForMeal(DietType mealSlot) {
		if (mealSlot == DietType.SNACK) {
			return SnackCategory.labels();
		}
		return MainMealCategory.labels();
	}

	// TODO: 가중치 값은 GPT 추천으로 논의되지 않았으나 추후 개선 예정
	private Weight weightByPurpose(Focus focus) {
		return switch (focus) {
			case DIET -> new Weight(
				1.5, 1.0, 0.9, 0.8, // kcal, carbs, protein, fat 가중치
				200, // kcal 초과 페널티
				50 // 탄단지 초과 페널티
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

	// DB에 저장된 추천을 응답 후보로 변환
	private FoodRecommendationCandidate toCandidate(RecommendedDiet r) {
		DietSourceType source = r.getSourceType() != null ? r.getSourceType() : DietSourceType.FOOD_DB;
		return new FoodRecommendationCandidate(
			r.getId(), // recommendationId
			r.getFoodId(),
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
	 * [Event] 식단 변경시 AI 피드백 비동기 생성 (트리거)
	 */
	@Async
	@Transactional
	public void generateDailyFeedback(String userId, LocalDate date) {
		try {
			User user = userRepository.findById(UUID.fromString(userId))
				.orElseThrow(UserNotFoundException::new);
			Focus focus = user.getFocus();
			DietType mealSlot = mealSlot(LocalTime.now(KOREA_ZONE));

			// 1. 오늘 누적값 확인
			NutrientTotals todayTotals = dietRecommendationMapper.findTotalsByDate(userId, date);
			boolean isFirstMeal = (todayTotals == null ||
				todayTotals.totalKcal() == null ||
				todayTotals.totalKcal().compareTo(BigDecimal.ZERO) == 0);

			NutritionGoals goals = nutritionGoalCalculator.calculateGoals(user);
			NutritionSummary nutritionSummary = isFirstMeal || todayTotals == null
				? NutritionSummary.empty()
				: nutritionStatusClassifier.classify(todayTotals, goals);

			// 2. AI 호출
			String feedback = dietAiFacade.feedbackDaily(isFirstMeal, mealSlot, focus, nutritionSummary);

			// 3. 저장 (기존 피드백 있으면 업데이트)
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
			throw new DietFeedbackGenerateException("데일리 피드백 생성 실패", e);
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
