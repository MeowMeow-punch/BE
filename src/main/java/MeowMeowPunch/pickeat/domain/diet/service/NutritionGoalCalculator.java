package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionGoals;
import MeowMeowPunch.pickeat.global.common.enums.ActivityLevel;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Diet][Service] 개인화된 영양 목표 계산기
 *
 * 사용자의 신체 정보(키, 몸무게, 나이, 성별), 활동량, 목표에 기반하여
 * 일일 영양 목표치를 4단계로 계산합니다.
 *
 * <p>
 * [계산 흐름]
 * <pre>
 * ┌─────────────────┐
 * │ 1. BMR 계산     │  Mifflin-St Jeor 공식
 * │ (기초대사량)    │  성별/체중/키/나이 기반
 * └────────┬────────┘
 *          ↓
 * ┌─────────────────┐
 * │ 2. TDEE 계산    │  BMR × 활동 계수
 * │ (활동대사량)    │  LOW(1.2) ~ VERYHIGH(1.725)
 * └────────┬────────┘
 *          ↓
 * ┌─────────────────┐
 * │ 3. Focus 보정   │  DIET(-500) / MUSCLE(+300) / HEALTHY(유지)
 * │ (목표 칼로리)   │  체중 감량/근성장/유지
 * └────────┬────────┘
 *          ↓
 * ┌─────────────────┐
 * │ 4. 영양소 분배  │  단백질(체중 기반) + 지방(25%) + 탄수화물(나머지)
 * │ (3대+8개 부가)  │  + 부가 영양소 8개 계산
 * └─────────────────┘
 * </pre>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionGoalCalculator {

	// 활동 계수 매핑
	private static final double ACTIVITY_MULTIPLIER_LOW = 1.2;
	private static final double ACTIVITY_MULTIPLIER_MEDIUM = 1.375;
	private static final double ACTIVITY_MULTIPLIER_HIGH = 1.55;
	private static final double ACTIVITY_MULTIPLIER_VERYHIGH = 1.725;

	// Focus별 칼로리 보정
	private static final int DIET_CALORIE_DEFICIT = 500;
	private static final int MUSCLE_CALORIE_SURPLUS = 300;

	// 단백질 목표 (g/kg)
	private static final double PROTEIN_RATIO_DIET = 1.8;
	private static final double PROTEIN_RATIO_MUSCLE = 2.1;
	private static final double PROTEIN_RATIO_HEALTHY = 1.35;

	// 지방 비율 (총 칼로리 대비)
	private static final double FAT_RATIO = 0.25;

	// 칼로리 변환 계수
	private static final int PROTEIN_KCAL_PER_GRAM = 4;
	private static final int CARBS_KCAL_PER_GRAM = 4;
	private static final int FAT_KCAL_PER_GRAM = 9;

	// 부가 영양소 고정값
	private static final int SUGAR_GOAL = 50; // g
	private static final int DIETARY_FIBER_PER_1000_KCAL = 14; // g
	private static final int VITAMIN_A_GOAL = 700; // μg
	private static final int VITAMIN_C_GOAL = 100; // mg
	private static final int VITAMIN_D_GOAL = 10; // μg
	private static final int CALCIUM_GOAL = 700; // mg
	private static final int IRON_GOAL_FEMALE = 18; // mg
	private static final int IRON_GOAL_MALE = 14; // mg
	private static final int SODIUM_GOAL = 2000; // mg (상한)

	/**
	 * [Main] 사용자 기반 영양 목표 계산
	 *
	 * @param user 사용자 엔티티 (신체 정보, 활동량, 목표 포함)
	 * @return 계산된 영양 목표
	 */
	public NutritionGoals calculateGoals(User user) {
		// 1단계: BMR 계산
		double bmr = calculateBMR(user);
		log.debug("[NutritionGoalCalculator] BMR calculated: {} kcal", bmr);

		// 2단계: TDEE 계산
		double tdee = calculateTDEE(bmr, user.getActivityLevel());
		log.debug("[NutritionGoalCalculator] TDEE calculated: {} kcal", tdee);

		// 3단계: Focus 기반 칼로리 보정
		int targetCalories = adjustCaloriesByFocus(tdee, user.getFocus());
		log.debug("[NutritionGoalCalculator] Target calories after focus adjustment: {} kcal", targetCalories);

		// 4단계: 3대 영양소 분배
		BigDecimal protein = calculateProtein(user.getWeight(), user.getFocus());
		BigDecimal fat = calculateFat(targetCalories);
		BigDecimal carbs = calculateCarbs(targetCalories, protein, fat);

		// 부가 영양소 계산
		int dietaryFiber = calculateDietaryFiber(targetCalories);
		int iron = calculateIron(user.getGender());

		log.info("[NutritionGoalCalculator] Goals calculated for user: kcal={}, protein={}g, carbs={}g, fat={}g",
			targetCalories, protein, carbs, fat);

		return new NutritionGoals(
			BigDecimal.valueOf(targetCalories),
			carbs,
			protein,
			fat,
			SUGAR_GOAL,
			dietaryFiber,
			VITAMIN_A_GOAL,
			VITAMIN_C_GOAL,
			VITAMIN_D_GOAL,
			CALCIUM_GOAL,
			iron,
			SODIUM_GOAL);
	}

	/**
	 * [Stage 1] BMR (기초대사량) 계산
	 *
	 * Mifflin-St Jeor 공식 사용:
	 * - 남성: BMR = 10 × 체중(kg) + 6.25 × 키(cm) − 5 × 나이 + 5
	 * - 여성: BMR = 10 × 체중(kg) + 6.25 × 키(cm) − 5 × 나이 − 161
	 *
	 * @param user 사용자 엔티티
	 * @return BMR (kcal)
	 */
	private double calculateBMR(User user) {
		int weight = user.getWeight();
		int height = user.getHeight();
		int age = user.getAge();
		Gender gender = user.getGender();

		double bmr = 10 * weight + 6.25 * height - 5 * age;

		if (gender == Gender.MALE) {
			bmr += 5;
		} else {
			bmr -= 161;
		}

		return bmr;
	}

	/**
	 * [Stage 2] TDEE (활동대사량) 계산
	 *
	 * BMR에 활동 계수를 곱하여 일일 총 에너지 소비량 산출:
	 * - LOW: 1.2 (거의 활동 없음)
	 * - MEDIUM: 1.375 (가벼운 활동)
	 * - HIGH: 1.55 (활동적)
	 * - VERYHIGH: 1.725 (운동선수 수준)
	 *
	 * @param bmr 기초대사량
	 * @param activityLevel 활동 수준
	 * @return TDEE (kcal)
	 */
	private double calculateTDEE(double bmr, ActivityLevel activityLevel) {
		double multiplier = switch (activityLevel) {
			case LOW -> ACTIVITY_MULTIPLIER_LOW;
			case MEDIUM -> ACTIVITY_MULTIPLIER_MEDIUM;
			case HIGH -> ACTIVITY_MULTIPLIER_HIGH;
			case VERYHIGH -> ACTIVITY_MULTIPLIER_VERYHIGH;
		};

		return bmr * multiplier;
	}

	/**
	 * [Stage 3] Focus 기반 칼로리 보정
	 *
	 * 목표에 따라 TDEE를 조정:
	 * - DIET: TDEE - 500 kcal (체중 감량, 주당 약 0.5kg)
	 * - MUSCLE: TDEE + 300 kcal (근성장, 근손실 최소화)
	 * - HEALTHY: TDEE 유지 (현재 체중 유지)
	 *
	 * @param tdee 활동대사량
	 * @param focus 목표
	 * @return 보정된 목표 칼로리
	 */
	private int adjustCaloriesByFocus(double tdee, Focus focus) {
		return switch (focus) {
			case DIET -> (int)Math.round(tdee - DIET_CALORIE_DEFICIT);
			case MUSCLE -> (int)Math.round(tdee + MUSCLE_CALORIE_SURPLUS);
			case HEALTHY -> (int)Math.round(tdee);
		};
	}

	/**
	 * [Stage 4-1] 단백질 목표 계산
	 *
	 * 체중 기반 단백질 섭취량 산출:
	 * - DIET: 1.8 g/kg (체중 감량 시 근손실 방지)
	 * - MUSCLE: 2.1 g/kg (근성장 촉진)
	 * - HEALTHY: 1.35 g/kg (일반 건강 유지)
	 *
	 * @param weight 체중 (kg)
	 * @param focus 목표
	 * @return 단백질 목표 (g)
	 */
	private BigDecimal calculateProtein(int weight, Focus focus) {
		double ratio = switch (focus) {
			case DIET -> PROTEIN_RATIO_DIET;
			case MUSCLE -> PROTEIN_RATIO_MUSCLE;
			case HEALTHY -> PROTEIN_RATIO_HEALTHY;
		};

		return BigDecimal.valueOf(weight * ratio).setScale(1, RoundingMode.HALF_UP);
	}

	/**
	 * [Stage 4-2] 지방 목표 계산
	 *
	 * 총 칼로리의 25%를 지방으로 배분:
	 * - 지방(g) = (총 칼로리 × 0.25) / 9
	 *
	 * @param totalCalories 목표 칼로리
	 * @return 지방 목표 (g)
	 */
	private BigDecimal calculateFat(int totalCalories) {
		double fatCalories = totalCalories * FAT_RATIO;
		double fatGrams = fatCalories / FAT_KCAL_PER_GRAM;
		return BigDecimal.valueOf(fatGrams).setScale(1, RoundingMode.HALF_UP);
	}

	/**
	 * [Stage 4-3] 탄수화물 목표 계산
	 *
	 * 나머지 칼로리를 탄수화물로 배분:
	 * - 탄수화물 kcal = 총 칼로리 − (단백질 kcal + 지방 kcal)
	 * - 탄수화물(g) = 탄수화물 kcal / 4
	 *
	 * @param totalCalories 목표 칼로리
	 * @param protein 단백질 목표 (g)
	 * @param fat 지방 목표 (g)
	 * @return 탄수화물 목표 (g)
	 */
	private BigDecimal calculateCarbs(int totalCalories, BigDecimal protein, BigDecimal fat) {
		double proteinKcal = protein.doubleValue() * PROTEIN_KCAL_PER_GRAM;
		double fatKcal = fat.doubleValue() * FAT_KCAL_PER_GRAM;
		double carbsKcal = totalCalories - proteinKcal - fatKcal;
		double carbsGrams = carbsKcal / CARBS_KCAL_PER_GRAM;
		return BigDecimal.valueOf(carbsGrams).setScale(1, RoundingMode.HALF_UP);
	}

	/**
	 * [Additional] 식이섬유 목표 계산
	 *
	 * 칼로리 비례 공식 사용:
	 * - 식이섬유(g) = 칼로리 / 1000 × 14
	 *
	 * @param totalCalories 목표 칼로리
	 * @return 식이섬유 목표 (g)
	 */
	private int calculateDietaryFiber(int totalCalories) {
		return (totalCalories / 1000) * DIETARY_FIBER_PER_1000_KCAL;
	}

	/**
	 * [Additional] 철분 목표 계산
	 *
	 * 성별 기반 권장량:
	 * - 여성: 18 mg (생리로 인한 철분 손실 고려)
	 * - 남성: 14 mg
	 *
	 * @param gender 성별
	 * @return 철분 목표 (mg)
	 */
	private int calculateIron(Gender gender) {
		return gender == Gender.FEMALE ? IRON_GOAL_FEMALE : IRON_GOAL_MALE;
	}
}
