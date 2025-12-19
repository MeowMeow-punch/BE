package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

public record TodayDietInfo(
	Long myDietId,
	String name,
	String mealType,
	int calorie,
	String time,
	boolean isEditable,
	Nutrients nutrients,
	List<String> thumbnailUrls
) {
	public static TodayDietInfo of(Long myDietId, String name, String mealType, int calorie, String time,
		boolean isEditable, Nutrients nutrients, List<String> thumbnailUrls) {
		return new TodayDietInfo(myDietId, name, mealType, calorie, time, isEditable, nutrients, thumbnailUrls);
	}
}
