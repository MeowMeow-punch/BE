package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// 웰스토리 오늘의 식단 카드 DTO
public record TodayRestaurantMenuInfo(
	String name,
	int calorie,
	String subName,
	int othersNum
) {
	public static TodayRestaurantMenuInfo of(String name, int calorie, String subName, int othersNum) {
		return new TodayRestaurantMenuInfo(name, calorie, subName, othersNum);
	}
}
