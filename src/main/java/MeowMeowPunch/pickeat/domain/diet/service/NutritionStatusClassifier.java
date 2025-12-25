package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionGoals;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionSummary;
import MeowMeowPunch.pickeat.global.common.enums.NutrientCode;

/**
 * [Diet][Service] 영양 상태 분류기
 *
 * - 일일 누적 섭취량 vs. 목표치 비율로 과잉/부족 판단
 * - 임계값: 부족 &lt; 90%, 적정 90~110%, 과잉 &gt;= 110% (중요 과잉 130%+)
 */
@Component
public class NutritionStatusClassifier {

	private static final BigDecimal DEFICIT_THRESHOLD = new BigDecimal("0.90");
	private static final BigDecimal MODERATE_EXCESS_THRESHOLD = new BigDecimal("1.10");
	private static final BigDecimal CRITICAL_EXCESS_THRESHOLD = new BigDecimal("1.30");
	private static final int DIVIDE_SCALE = 4;

	public NutritionSummary classify(NutrientTotals totals, NutritionGoals goals) {
		if (totals == null || goals == null) {
			return NutritionSummary.empty();
		}

		List<NutrientCode> criticalExcess = new ArrayList<>();
		List<NutrientCode> moderateExcess = new ArrayList<>();
		List<NutrientCode> deficit = new ArrayList<>();
		List<NutrientCode> adequate = new ArrayList<>();

		classifyAndCollect(totals.totalKcal(), goals.kcal(), NutrientCode.KCAL, criticalExcess, moderateExcess,
			deficit, adequate);
		classifyAndCollect(totals.totalCarbs(), goals.carbs(), NutrientCode.CARBS, criticalExcess, moderateExcess,
			deficit, adequate);
		classifyAndCollect(totals.totalProtein(), goals.protein(), NutrientCode.PROTEIN, criticalExcess,
			moderateExcess, deficit, adequate);
		classifyAndCollect(totals.totalFat(), goals.fat(), NutrientCode.FAT, criticalExcess, moderateExcess, deficit,
			adequate);
		classifyAndCollect(totals.totalSodium(), BigDecimal.valueOf(goals.sodium()), NutrientCode.SODIUM,
			criticalExcess, moderateExcess, deficit, adequate);

		return new NutritionSummary(
			List.copyOf(criticalExcess),
			List.copyOf(moderateExcess),
			List.copyOf(deficit),
			List.copyOf(adequate)
		);
	}

	private void classifyAndCollect(BigDecimal total, BigDecimal goal, NutrientCode nutrient,
		List<NutrientCode> criticalExcess, List<NutrientCode> moderateExcess, List<NutrientCode> deficit,
		List<NutrientCode> adequate) {

		if (goal == null || goal.compareTo(BigDecimal.ZERO) <= 0 || total == null) {
			return;
		}

		BigDecimal ratio = safeDivide(total, goal);

		if (ratio.compareTo(CRITICAL_EXCESS_THRESHOLD) >= 0) {
			criticalExcess.add(nutrient);
			return;
		}
		if (ratio.compareTo(MODERATE_EXCESS_THRESHOLD) >= 0) {
			moderateExcess.add(nutrient);
			return;
		}
		if (ratio.compareTo(DEFICIT_THRESHOLD) < 0) {
			deficit.add(nutrient);
			return;
		}
		adequate.add(nutrient);
	}

	private BigDecimal safeDivide(BigDecimal total, BigDecimal goal) {
		if (goal.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return total.divide(goal, DIVIDE_SCALE, RoundingMode.HALF_UP);
	}
}
