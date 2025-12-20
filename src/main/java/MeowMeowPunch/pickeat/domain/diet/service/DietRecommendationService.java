package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDietFood;
import MeowMeowPunch.pickeat.domain.diet.exception.DietRecommendationSaveException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.FoodBaseUnit;
import MeowMeowPunch.pickeat.global.common.enums.MainMealCategory;
import MeowMeowPunch.pickeat.global.common.enums.SnackCategory;
import MeowMeowPunch.pickeat.welstory.entity.RestaurantMapping;
import MeowMeowPunch.pickeat.welstory.repository.RestaurantMappingRepository;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietRecommendationService {
	private static final int TOP_LIMIT = 6;
	private static final int MIN_PICK = 1;
	private static final int MAX_PICK = 2;
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
	private final RestaurantMappingRepository restaurantMappingRepository;
	private final WelstoryMenuService welstoryMenuService;

	// TODO: User 테이블 연동 시 제거 예정 (임시 Group 여부/식당명)
	private final UserStatus mockUserStatus = UserStatus.GROUP;
	private final String mockGroupName = "전기부산";

	/**
	 * 오늘 날짜와 현재 식사 시간대에 맞춰 추천 TOP5를 산출한다.
	 * - 이미 해당 시간대 추천이 저장돼 있으면 DB값을 재사용
	 * - 없으면 오늘 섭취 합계 → 남은 영양소 → 목적별 가중치로 계산해 TOP5 조회
	 * - 상위 2개는 추천 테이블에 저장
	 */
	@Transactional
	public List<FoodRecommendationCandidate> recommendTopFoods(String userId, Focus purposeType,
		NutrientTotals totals) {
		LocalDate today = LocalDate.now(KOREA_ZONE);
		LocalTime nowTime = LocalTime.now(KOREA_ZONE);
		DietType mealSlot = mealSlot(nowTime);

		List<RecommendedDiet> existing = recommendedDietRepository.findByUserIdAndDateAndDietTypeOrderByCreatedAtDesc(
			userId, today, mealSlot);

		// 오늘 날짜로 DietStatus(아침, 점심, 저녁, 간식) 추천이 1개 이상 있으면 바로 반환
		if (existing.size() >= MIN_PICK) {
			return existing.stream()
				.map(this::toCandidate)
				.toList();
		}

		if (isGroupUser(userId) && mealSlot == DietType.LUNCH) {
			List<FoodRecommendationCandidate> groupLunch = recommendWelstoryLunch(today, purposeType, totals);
			if (!groupLunch.isEmpty()) {
				try {
					List<RecommendedDiet> saved = saveTopRecommended(userId, today, mealSlot, groupLunch);
					return saved.stream().map(this::toCandidate).toList();
				} catch (Exception e) {
					throw new DietRecommendationSaveException(e);
				}
			}
		}

		// 1) 끼니별 목표 영양분 계산
		BigDecimal targetMealKcal = targetForMeal(GOAL_KCAL, totals.totalKcal(), mealSlot);
		BigDecimal targetMealCarbs = targetMacroForMeal(GOAL_CARBS, totals.totalCarbs(), mealSlot);
		BigDecimal targetMealProtein = targetMacroForMeal(GOAL_PROTEIN, totals.totalProtein(), mealSlot);
		BigDecimal targetMealFat = targetMacroForMeal(GOAL_FAT, totals.totalFat(), mealSlot);
		List<String> allowedCategories = allowedCategoriesForMeal(mealSlot);

		// 2) 목적별 가중치/패널티 설정
		Weight weight = weightByPurpose(purposeType);

		// 3) 남은 영양분 기반 TOP 후보 생성 (AI가 1~2개 선택 예정)
		List<FoodRecommendationCandidate> candidates = dietRecommendationMapper.findTopFoodCandidates(
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

		// TODO: AI 선택 연동 후 결과 개수(1~2)에 맞게 저장하도록 수정
		try {
			List<RecommendedDiet> saved = saveTopRecommended(userId, today, mealSlot,
				candidates.stream().limit(MAX_PICK).toList());
			return saved.stream().map(this::toCandidate).toList();
		} catch (Exception e) {
			throw new DietRecommendationSaveException(e);
		}
	}

	private List<FoodRecommendationCandidate> recommendWelstoryLunch(LocalDate targetDate, Focus focus,
		NutrientTotals totals) {
		String restaurantId = restaurantMappingRepository.findByRestaurantName(mockGroupName)
			.map(RestaurantMapping::getRestaurantId)
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

		// 목표 영양 기반으로 점수화 후 상위 2개 선택
		BigDecimal targetMealKcal = targetForMeal(GOAL_KCAL, totals.totalKcal(), DietType.LUNCH);
		BigDecimal targetMealCarbs = targetMacroForMeal(GOAL_CARBS, totals.totalCarbs(), DietType.LUNCH);
		BigDecimal targetMealProtein = targetMacroForMeal(GOAL_PROTEIN, totals.totalProtein(), DietType.LUNCH);
		BigDecimal targetMealFat = targetMacroForMeal(GOAL_FAT, totals.totalFat(), DietType.LUNCH);
		Weight weight = weightByPurpose(focus);

		return menus.stream()
			.map(m -> scoreCandidate(m, targetMealKcal, targetMealCarbs, targetMealProtein, targetMealFat, weight))
			.sorted((a, b) -> Double.compare(b.score(), a.score()))
			.limit(MAX_PICK)
			.toList();
	}

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
			recommendedDietFoodRepository.save(
				RecommendedDietFood.builder()
					.recommendedDiet(saved)
					.foodId(foodId)
					.quantity(1)
					.build()
			);
			return saved;
		}).toList();
	}

	private Long resolveFoodId(FoodRecommendationCandidate c) {
		if (c.foodId() != null && c.foodId() > 0) {
			return c.foodId();
		}
		Food existing = foodRepository.findByName(c.name());
		if (existing != null) {
			return existing.getId();
		}
		Food created = foodRepository.save(
			Food.builder()
				.foodCode(null)
				.name(c.name())
				.category(null)
				.baseAmount(200)
				.baseUnit(FoodBaseUnit.G)
				.servingSize(null)
				.servingDesc(null)
				.kcal(nullSafe(c.kcal()))
				.carbs(nullSafe(c.carbs()))
				.protein(nullSafe(c.protein()))
				.fat(nullSafe(c.fat()))
				.sugar(BigDecimal.ZERO)
				.dietaryFiber(BigDecimal.ZERO)
				.vitA(BigDecimal.ZERO)
				.vitC(BigDecimal.ZERO)
				.vitD(BigDecimal.ZERO)
				.calcium(BigDecimal.ZERO)
				.iron(BigDecimal.ZERO)
				.sodium(BigDecimal.ZERO)
				.thumbnailUrl(c.thumbnailUrl() == null ? "" : c.thumbnailUrl())
				.build()
		);
		return created.getId();
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
			case BALANCE -> new Weight(
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
			source
		);
	}

	// 가중치 dto
	private record Weight(
		double kcal,
		double carbs,
		double protein,
		double fat,
		double penaltyOverKcal,
		double penaltyOverMacro
	) {
	}

	private enum UserStatus {
		SINGLE, GROUP
	}
}
