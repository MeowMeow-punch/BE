package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// 식단 페이지의 상세 영양분 묶음
public record NutritionInfo(
	NutritionDetail sugar,
	NutritionDetail dietaryFiber,
	NutritionDetail vitaminA,
	NutritionDetail vitaminC,
	NutritionDetail vitaminD,
	NutritionDetail calcium,
	NutritionDetail iron,
	NutritionDetail sodium
) {
	public static NutritionInfo of(
		NutritionDetail sugar,
		NutritionDetail dietaryFiber,
		NutritionDetail vitaminA,
		NutritionDetail vitaminC,
		NutritionDetail vitaminD,
		NutritionDetail calcium,
		NutritionDetail iron,
		NutritionDetail sodium
	) {
		return new NutritionInfo(sugar, dietaryFiber, vitaminA, vitaminC, vitaminD, calcium, iron, sodium);
	}
}
