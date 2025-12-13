package MeowMeowPunch.pickeat.global.common.dto.response;

public record TodayDietInfo(
	Long myDietId,
	String name,
	String mealType,
	int calorie,
	String time,
	Nutrients nutrients,
	String thumbnailUrl
) {
	public static TodayDietInfo of(Long myDietId, String name, String mealType, int calorie, String time,
		Nutrients nutrients, String thumbnailUrl) {
		return new TodayDietInfo(myDietId, name, mealType, calorie, time, nutrients, thumbnailUrl);
	}
}
