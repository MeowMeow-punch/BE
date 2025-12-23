package MeowMeowPunch.pickeat.domain.diet.dto;

import java.math.BigDecimal;

/**
 * [Diet][DTO] 개인화된 영양 목표
 *
 * 사용자의 신체 정보, 활동량, 목표에 기반하여 계산된 일일 영양 목표치를 담는 불변 객체.
 * 매 요청마다 동적으로 계산되며 DB에 저장하지 않음.
 *
 * <p>
 * [계산 단계]
 * <pre>
 * 1. BMR (기초대사량) - Mifflin-St Jeor 공식
 * 2. TDEE (활동대사량) - BMR × 활동 계수
 * 3. Focus 보정 - DIET(-500), MUSCLE(+300), HEALTHY(유지)
 * 4. 영양소 분배 - 단백질(체중 기반), 지방(25%), 탄수화물(나머지)
 * </pre>
 * </p>
 *
 * @param kcal 목표 칼로리 (kcal)
 * @param carbs 목표 탄수화물 (g)
 * @param protein 목표 단백질 (g)
 * @param fat 목표 지방 (g)
 * @param sugar 목표 당류 (g) - 고정 50g
 * @param dietaryFiber 목표 식이섬유 (g) - 칼로리 비례
 * @param vitaminA 목표 비타민 A (μg) - 고정 700μg
 * @param vitaminC 목표 비타민 C (mg) - 고정 100mg
 * @param vitaminD 목표 비타민 D (μg) - 고정 10μg
 * @param calcium 목표 칼슘 (mg) - 고정 700mg
 * @param iron 목표 철분 (mg) - 성별 기반 (여성 18mg, 남성 14mg)
 * @param sodium 목표 나트륨 (mg) - 고정 2000mg (상한)
 */
public record NutritionGoals(
	BigDecimal kcal,
	BigDecimal carbs,
	BigDecimal protein,
	BigDecimal fat,
	int sugar,
	int dietaryFiber,
	int vitaminA,
	int vitaminC,
	int vitaminD,
	int calcium,
	int iron,
	int sodium
) {
}
