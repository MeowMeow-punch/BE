package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

// 식단 상세 응답 DTO
public record DietInfo(
	Long myDietId,
	String mealType,
	String time,
	String date,
	List<DietFoodItem> foods
) {
	public static DietInfo of(Long myDietId, String mealType, String time, String date, List<DietFoodItem> foods) {
		return new DietInfo(myDietId, mealType, time, date, foods);
	}
}
