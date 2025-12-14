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
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.domain.diet.exception.DietRecommendationSaveException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
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

	/**
	 * 오늘 날짜와 현재 식사 시간대에 맞춰 추천 TOP5를 산출한다.
	 * - 이미 해당 시간대 추천이 저장돼 있으면 DB값을 재사용
	 * - 없으면 오늘 섭취 합계 → 남은 영양소 → 목적별 가중치로 계산해 TOP5 조회
	 * - 상위 2개는 추천 테이블에 저장 (추후 AI 선택으로 교체 예정)
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
			weight.kcal,
			weight.carbs,
			weight.protein,
			weight.fat,
			weight.penaltyOverKcal,
			weight.penaltyOverMacro,
			KCAL_TOLERANCE,
			BASE_UNIT_GRAM,
			TOP_LIMIT
		);

		// TODO: AI 선택 연동 후 결과 개수(1~2)에 맞게 저장하도록 수정
		try {
			saveTopRecommended(userId, today, mealSlot, candidates.stream().limit(MAX_PICK).toList());
		} catch (Exception e) {
			throw new DietRecommendationSaveException(e);
		}

		return candidates;
	}

	// RecommendedDiet 테이블에 저장
	private void saveTopRecommended(String userId, LocalDate date, DietType dietType,
		List<FoodRecommendationCandidate> picks) {
		for (FoodRecommendationCandidate c : picks) {
			RecommendedDiet entity = RecommendedDiet.builder()
				.userId(userId)
				.foodId(c.foodId())
				.dietType(dietType)
				.date(date)
				.title(c.name())
				.kcal(nullSafe(c.kcal()))
				.carbs(nullSafe(c.carbs()))
				.protein(nullSafe(c.protein()))
				.fat(nullSafe(c.fat()))
				.thumbnailUrl(c.thumbnailUrl())
				.build();
			recommendedDietRepository.save(entity);
		}
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
			return List.of(
				"빵 및 과자류",
				"유제품류 및 빙과류",
				"음료 및 차류",
				"과일류"
			);
		}
		return List.of(
			"밥류",
			"찜류",
			"구이류",
			"볶음류",
			"조림류",
			"튀김류",
			"찌개 및 전골류",
			"국 및 탕류",
			"면 및 만두류",
			"수·조·어·육류",
			"두류, 견과 및 종실류"
		);
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
		return new FoodRecommendationCandidate(
			r.getFoodId(),
			r.getTitle(),
			r.getThumbnailUrl(),
			nullSafe(r.getKcal()),
			nullSafe(r.getCarbs()),
			nullSafe(r.getProtein()),
			nullSafe(r.getFat()),
			null,
			0.0 // 이미 저장된 추천은 점수 없음
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
}
