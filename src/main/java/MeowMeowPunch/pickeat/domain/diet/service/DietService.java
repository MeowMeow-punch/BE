package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.response.AiFeedBack;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidPurposeTypeException;
import MeowMeowPunch.pickeat.domain.diet.exception.MissingDietUserIdException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {
	// TODO: 유저 테이블 생성되면 삭제 예정
	private static final int GOAL_KCAL = 2000;
	private static final int GOAL_CARBS = 280;
	private static final int GOAL_PROTEIN = 120;
	private static final int GOAL_FAT = 70;

	private final DietRecommendationMapper dietRecommendationMapper;
	private final DietRecommendationService dietRecommendationService;

	// 홈 페이지 조회 (오늘 기준)
	public DietHomeResponse getHome(String userId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		// TODO: focus 를 사용자 테이블에서 가져올 예정
		Focus focus = parseFocus("BALANCE");

		// 오늘 섭취 합계 (쿼리 1회)
		NutrientTotals totals = dietRecommendationMapper.findTodayTotals(userId);

		// 식단 추천 계산 트리거 (이미 있으면 재사용) 후 TOP5 후보 반환
		List<FoodRecommendationCandidate> recommendedCandidates = dietRecommendationService.recommendTopFoods(userId,
			focus, totals);

		int currentKcal = toInt(nullSafe(totals.totalKcal()));
		int currentCarbs = toInt(nullSafe(totals.totalCarbs()));
		int currentProtein = toInt(nullSafe(totals.totalProtein()));
		int currentFat = toInt(nullSafe(totals.totalFat()));

		SummaryInfo summaryInfo = SummaryInfo.of(
			SummaryInfo.Calorie.of(currentKcal, GOAL_KCAL),
			SummaryInfo.NutrientInfo.of(currentCarbs, GOAL_CARBS, status(currentCarbs, GOAL_CARBS)),
			SummaryInfo.NutrientInfo.of(currentProtein, GOAL_PROTEIN, status(currentProtein, GOAL_PROTEIN)),
			SummaryInfo.NutrientInfo.of(currentFat, GOAL_FAT, status(currentFat, GOAL_FAT))
		);

		// TODO: AI 연결 예정
		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI 피드백은 준비 중입니다.",
			LocalDateTime.now().withNano(0).toString()
		);

		List<RecommendedDietInfo> recommended = recommendedCandidates.stream()
			.limit(2)
			.map(c -> RecommendedDietInfo.of(
				c.foodId(),
				c.name(),
				mealSlot(LocalTime.now()).name(),
				c.thumbnailUrl(),
				toInt(c.kcal())
			))
			.toList();

		return DietHomeResponse.of(summaryInfo, aiFeedBack, recommended);
	}

	// 목적
	private Focus parseFocus(String raw) {
		if (!StringUtils.hasText(raw)) {
			return Focus.BALANCE;
		}
		try {
			return Focus.valueOf(raw.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidPurposeTypeException(raw);
		}
	}
}
