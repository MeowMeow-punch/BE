package MeowMeowPunch.pickeat.global.common.dto.response;

public record WeeklyCaloriesInfo(
	String day,
	int calorie
) {
	public static WeeklyCaloriesInfo of(String day, int calorie) {
		return new WeeklyCaloriesInfo(day, calorie);
	}
}
