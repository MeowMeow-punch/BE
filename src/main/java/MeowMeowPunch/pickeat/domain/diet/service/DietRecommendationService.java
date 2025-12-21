package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

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
import MeowMeowPunch.pickeat.domain.diet.exception.DietRecommendationSaveException;
import MeowMeowPunch.pickeat.domain.diet.exception.FoodNotFoundException;
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
import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;
import MeowMeowPunch.pickeat.welstory.repository.GroupMappingRepository;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;
import lombok.RequiredArgsConstructor;

/**
 * [Diet][Service] 식단 추천 계산 및 저장 서비스.
 *
 * - 그룹/개인 상황에 맞는 추천 후보 조회
 * - 추천 식단 엔티티 저장 및 스코어링
 * - AI 추천 연동
 */
@Service
@RequiredArgsConstructor
public class DietRecommendationService {
	private static final int TOP_LIMIT = 6;
	private static final int MIN_PICK = 1;
	// private static final int MAX_PICK = 2; // AI가 결정하므로 Facade에서 제어
	private static final int KCAL_TOLERANCE = 200; // +- 칼로리 기준
	private static final String BASE_UNIT_GRAM = "G";
	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	private static final String WELSTORY_LUNCH_ID = "2";
	private static final String WELSTORY_LUNCH_NAME = "점심";
	// 임시 목표값 (추후 사용자 정보 기반 계산)
	private static final BigDecimal GOAL_KCAL = BigDecimal.valueOf(2000);
	private static final BigDecimal GOAL_CARBS = BigDecimal.valueOf(280);
	private static final BigDecimal GOAL_PROTEIN = BigDecimal.valueOf(120);
	private static final BigDecimal GOAL_FAT = BigDecimal.valueOf(70);

	private static final Map<DietType, BigDecimal> MEAL_RATIO = Map.of(
		DietType.BREAKFAST, new BigDecimal("0.30"),
		DietType.LUNCH, new BigDecimal("0.30"),
		DietType.DINNER, new BigDecimal("0.30"),
		DietType.SNACK, new BigDecimal("0.10")
	);

	private final DietRecommendationMapper dietRecommendationMapper;
	private final RecommendedDietRepository recommendedDietRepository;
	private final RecommendedDietFoodRepository recommendedDietFoodRepository;
	private final FoodRepository foodRepository;
	private final DietRepository dietRepository;
	private final GroupMappingRepository groupMappingRepository;
	private final WelstoryMenuService welstoryMenuService;
	private final DietAiFacade dietAiFacade;
	private final AiFeedBackRepository aiFeedBackRepository;

	// TODO: User 테이블 연동 시 제거 예정 (임시 Group 여부/식당명)
	private final UserStatus mockUserStatus = UserStatus.GROUP;
	private final String mockGroupName = "전기부산";

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
		LocalDate today = LocalDate.now(KOREA_ZONE);
		LocalTime nowTime = LocalTime.now(KOREA_ZONE);
		DietType mealSlot = mealSlot(nowTime);

		// 1. 이미 생성된 추천 조회
		List<RecommendedDiet> existing = recommendedDietRepository.findByUserIdAndDateAndDietTypeOrderByCreatedAtDesc(
			userId, today, mealSlot);

		if (existing.size() >= MIN_PICK) {
			List<FoodRecommendationCandidate> picks = existing.stream()
				.map(this::toCandidate)
				.toList();

			// 저장된 피드백 사유 조회
			String reason = aiFeedBackRepository.findByUserIdAndDateAndType(userId, today, FeedBackType.RECOMMENDATION)
				.map(AiFeedBack::getContent)
				.orElse("목표 영양에 근접한 메뉴를 우선 추천했어요.");

			return HomeRecommendationResult.of(picks, reason);
		}

		List<FoodRecommendationCandidate> candidates;

		// 2. 웰스토리(그룹) 점심 우선 확인
		if (isGroupUser(userId) && mealSlot == DietType.LUNCH) {
			candidates = recommendWelstoryLunch(today, focus, totals);
		} else {
			// 3. 일반 배달/식당(Food DB) 후보 생성
			candidates = recommendGeneralFoods(mealSlot, focus, totals);
		}

		// 4. AI 호출하여 최종 Pick & Reason 획득
		HomeRecommendationResult aiResult = dietAiFacade.recommendHome(focus, mealSlot, candidates);

		// 5. 저장
		try {
			// 선택된 메뉴 저장
			saveTopRecommended(userId, today, mealSlot, aiResult.picks());

			// AI 이유 저장 (Daily Recommendation Feedback)
			// 기존에 같은 날짜/타입의 피드백이 있다면 업데이트
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
			TOP_LIMIT
		);
	}

	/**
	 * [Recommend] 웰스토리 점심 식단 후보 조회 및 점수화
	 */
	private List<FoodRecommendationCandidate> recommendWelstoryLunch(LocalDate targetDate, Focus focus,
		NutrientTotals totals) {
		String restaurantId = groupMappingRepository.findByGroupName(mockGroupName)
			.map(GroupMapping::getGroupId)
			.orElse(null);
		if (restaurantId == null) {
			return List.of();
		}

		int dateYyyymmdd = toYyyymmdd(targetDate);

		List<FoodRecommendationCandidate> menus = welstoryMenuService.getRecommendationCandidates(
			restaurantId, dateYyyymmdd, WELSTORY_LUNCH_ID, WELSTORY_LUNCH_NAME);
		if (menus.isEmpty()) {
			return List.of();
		}

		BigDecimal targetMealKcal = targetForMeal(GOAL_KCAL, totals.totalKcal(), DietType.LUNCH);
		BigDecimal targetMealCarbs = targetMacroForMeal(GOAL_CARBS, totals.totalCarbs(), DietType.LUNCH);
		BigDecimal targetMealProtein = targetMacroForMeal(GOAL_PROTEIN, totals.totalProtein(), DietType.LUNCH);
		BigDecimal targetMealFat = targetMacroForMeal(GOAL_FAT, totals.totalFat(), DietType.LUNCH);
		Weight weight = weightByPurpose(focus);

		// 후보 생성 단계에서는 일단 정렬해서 넘겨줌 (AI 참고용)
		return menus.stream()
			.map(m -> scoreCandidate(m, targetMealKcal, targetMealCarbs, targetMealProtein, targetMealFat, weight))
			.sorted((a, b) -> Double.compare(b.score(), a.score()))
			.toList();
	}

	/**
	 * [Score] 웰스토리 후보와 목표 영양 차이를 가중치로 점수화
	 */
	private FoodRecommendationCandidate scoreCandidate(FoodRecommendationCandidate c, BigDecimal targetKcal,
		BigDecimal targetCarb, BigDecimal targetProtein, BigDecimal targetFat, Weight weight) {
		double kcalDiff = diff(c.kcal(), targetKcal) * weight.kcal();
		double carbDiff = diff(c.carbs(), targetCarb) * weight.carbs();
		double proteinDiff = diff(c.protein(), targetProtein) * weight.protein();
		double fatDiff = diff(c.fat(), targetFat) * weight.fat();

		double penalty = kcalDiff + carbDiff + proteinDiff + fatDiff;
		double score = -penalty; // 차이가 작을수록 높은 점수

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
			DietSourceType.WELSTORY
		);
	}

	private double diff(BigDecimal value, BigDecimal target) {
		if (value == null || target == null) {
			return Double.MAX_VALUE / 4;
		}
		return value.subtract(target).abs().doubleValue();
	}

	private boolean isGroupUser(String userId) {
		return mockUserStatus == UserStatus.GROUP;
	}

	private int toYyyymmdd(LocalDate date) {
		return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
	}

	// RecommendedDiet 테이블에 저장
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
					.build()

			);
			if (foodId != null) {
				recommendedDietFoodRepository.save(
					RecommendedDietFood.builder()
						.recommendedDiet(saved)
						.foodId(foodId)
						.quantity(2)
						.build()
				);
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

	// 한 끼 타겟 산출 (음수 방지)
	private BigDecimal remainingDaily(BigDecimal goal, BigDecimal eaten) {
		BigDecimal r = goal.subtract(nullSafe(eaten));
		return r.max(BigDecimal.ZERO);
	}

	// 칼로리 타겟 값 측정
	private BigDecimal targetForMeal(BigDecimal dailyGoal, BigDecimal eatenSoFar, DietType mealSlot) {
		BigDecimal remaining = remainingDaily(dailyGoal, eatenSoFar); // 오늘 남은 잔여 칼로리
		BigDecimal slotGoal = dailyGoal.multiply(
			MEAL_RATIO.getOrDefault(mealSlot, new BigDecimal("0.25"))); // 해당 끼니의 이상적 목표량
		BigDecimal rawTarget = slotGoal.min(remaining); // 이번 끼니가 slotGoal 과 남은 잔여량을 넘을 수 없도록 하기위해 둘 중 더 작은 값을 택한다
		BigDecimal min = new BigDecimal("250");
		BigDecimal max = new BigDecimal("800");
		return rawTarget.max(min).min(max); // 상한, 하한을 고정
	}

	// 영양분 타겟 값 측정
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

	// TODO: 가중치 값은 GPT 추천으로 임의로 지정했고 추후 개선할 예정
	private Weight weightByPurpose(Focus focus) {
		return switch (focus) {
			case DIET -> new Weight(
				1.5, 1.0, 0.9, 0.8, // kcal, carbs, protein, fat 가중치
				200, // kcal 초과 패널티
				50   // 탄단지 초과 패널티
			);
			case BULK_UP -> new Weight(
				1.0, 0.9, 1.5, 0.9,
				80,
				40
			);
			case HEALTH -> new Weight(
				1.0, 1.0, 1.0, 1.0,
				100,
				50
			);
		};
	}

	// DB에 저장된 추천을 응답용 후보로 변환
	private FoodRecommendationCandidate toCandidate(RecommendedDiet r) {
		DietSourceType source = r.getSourceType() != null ? r.getSourceType() : DietSourceType.FOOD_DB;
		return new FoodRecommendationCandidate(
				r.getId(), // dietId를 candidate의 id 슬롯으로 전달해 DietService에서 사용
				r.getTitle(),
				r.getThumbnailUrl(),
				nullSafe(r.getKcal()),
				nullSafe(r.getCarbs()),
				nullSafe(r.getProtein()),
				nullSafe(r.getFat()),
				null,
				0.0, // 이미 저장된 추천은 점수 없음
				source);
	}

	/**
	 * [Event] 식단 변경 시 AI 피드백 비동기 생성 및 저장
	 */
	@Async
	@Transactional
	public void generateDailyFeedback(String userId, LocalDate date) {
		try {
			// 1. 오늘 섭취량 확인
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

			// 3. AI 호출
			String feedback = dietAiFacade.feedbackDaily(isFirstMeal, todayTotals, lastRecord);

			// 4. 저장 (기존 피드백 있으면 업데이트)
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
			// 비동기 실행 중 에러는 로그만 남기고 무시 (메인 트랜잭션 영향 X)
			// log.error("Faild to generate daily feedback", e);
			System.err.println("Failed to generate daily feedback: " + e.getMessage());
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

	// TODO: user 기반시 삭제
	private enum UserStatus {
		SINGLE, GROUP
	}
}
