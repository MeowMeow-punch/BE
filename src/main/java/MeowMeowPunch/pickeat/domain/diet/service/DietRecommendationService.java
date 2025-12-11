package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.enums.DietStatus;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietRecommendationService {
	private static final double PORTION_FACTOR = 2.0; // 200g 기준
	private static final int TOP_LIMIT = 5;
	private static final int KCAL_TOLERANCE = 400; // +- 칼로리 기준

	// 임시 목표값 (추후 사용자 정보 기반 계산)
	private static final BigDecimal GOAL_KCAL = BigDecimal.valueOf(2000);
	private static final BigDecimal GOAL_CARBS = BigDecimal.valueOf(280);
	private static final BigDecimal GOAL_PROTEIN = BigDecimal.valueOf(120);
	private static final BigDecimal GOAL_FAT = BigDecimal.valueOf(70);

	private final DietRecommendationMapper dietRecommendationMapper;
	private final RecommendedDietRepository recommendedDietRepository;

	/**
	 * 오늘 날짜와 현재 식사 시간대에 맞춰 추천 TOP5를 산출한다.
	 * - 이미 해당 시간대 추천이 저장돼 있으면 DB값을 재사용
	 * - 없으면 오늘 섭취 합계 → 남은 영양소 → 목적별 가중치로 계산해 TOP5 조회
	 * - 상위 2개는 추천 테이블에 저장 (추후 AI 선택으로 교체 예정)
	 */
	@Transactional
	public List<FoodRecommendationCandidate> recommendTopFoods(String userId, Focus focus,
		NutrientTotals totals) {
		LocalDate today = LocalDate.now();
		DietStatus mealSlot = mealSlot(LocalTime.now());

		List<RecommendedDiet> existing = recommendedDietRepository.findByUserIdAndDateAndDietStatusOrderByCreatedAtDesc(
			userId, today, mealSlot);

		// 오늘 날짜로 DietStatus(아침, 점심, 저녁, 간식) 있다면 바로 반환
		if (!existing.isEmpty()) {
			return existing.stream()
				.map(this::toCandidate)
				.toList();
		}

		// 1) 오늘 남은 여유 영양분 계산
		BigDecimal remainingKcal = remaining(GOAL_KCAL, totals.totalKcal());
		BigDecimal remainingCarbs = remaining(GOAL_CARBS, totals.totalCarbs());
		BigDecimal remainingProtein = remaining(GOAL_PROTEIN, totals.totalProtein());
		BigDecimal remainingFat = remaining(GOAL_FAT, totals.totalFat());

		// 2) 목적별 가중치/패널티 설정
		Weight weight = weightByFocus(focus);

		// 3) 남은 영양분 기반 top5 식단 추천 (포션 200g 기준)
		List<FoodRecommendationCandidate> candidates = dietRecommendationMapper.findTopFoodCandidates(
			remainingKcal,
			remainingCarbs,
			remainingProtein,
			remainingFat,
			weight.kcal,
			weight.carbs,
			weight.protein,
			weight.fat,
			weight.penaltyOverKcal,
			weight.penaltyOverMacro,
			PORTION_FACTOR,
			KCAL_TOLERANCE,
			TOP_LIMIT
		);

		// TODO: 추후 AI가 TOP 5 중 2개를 선택하도록 연동할 예정
		saveTopRecommended(userId, today, mealSlot, candidates.stream().limit(2).toList());

		return candidates;
	}

	// RecommendedDiet 테이블에 저장
	private void saveTopRecommended(String userId, LocalDate date, DietStatus dietStatus,
		List<FoodRecommendationCandidate> picks) {
		for (FoodRecommendationCandidate c : picks) {
			RecommendedDiet entity = RecommendedDiet.builder()
				.userId(userId)
				.foodId(c.foodId())
				.dietStatus(dietStatus)
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

	// 섭취해야할 영양분 계산
	private BigDecimal remaining(BigDecimal goal, BigDecimal eaten) {
		return goal.subtract(nullSafe(eaten));
	}

	// TODO: 가중치 값은 GPT 추천으로 임의로 지정했고 추후 개선할 예정
	private Weight weightByFocus(Focus focus) {
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
