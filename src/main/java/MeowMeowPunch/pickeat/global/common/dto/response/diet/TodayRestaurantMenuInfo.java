package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// 웰스토리 오늘의 식단 카드 DTO
public record TodayRestaurantMenuInfo(
	String name,
	int calorie,
	String subName
) {
	public static TodayRestaurantMenuInfo of(String name, int calorie, String subName) {
		return new TodayRestaurantMenuInfo(name, calorie, subName);
	}
}
