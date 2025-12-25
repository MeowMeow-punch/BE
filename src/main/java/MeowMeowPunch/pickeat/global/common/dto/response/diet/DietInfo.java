package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

// 식단 상세 응답 DTO
public record DietInfo(
	Long myDietId,
	String title,
	String mealType,
	String time,
	String date,
	boolean isEditable,
	int calorie,
	Nutrients nutrients,
	List<DietDetailItem> foods,
	List<String> thumbnailUrls
) {
	public static DietInfo of(Long myDietId, String title, String mealType, String time, String date,
		boolean isEditable, int calorie, Nutrients nutrients, List<DietDetailItem> foods, List<String> thumbnailUrls) {
		return new DietInfo(myDietId, title, mealType, time, date, isEditable, calorie, nutrients, foods, thumbnailUrls);
	}
}
