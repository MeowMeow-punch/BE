package MeowMeowPunch.pickeat.welstory.dto;

// 웰스토리 식단 항목 DTO (image URL 조합 포함)
public record WelstoryMenuItem(
	String restaurantId,
	int dateYyyymmdd,
	String mealTimeId,
	String mealTimeName,
	String name,
	String course,
	String courseName,
	String submenu,
	String kcal,
	String photoUrl,
	String hallNo,
	String menuCourseType
) {
	public static WelstoryMenuItem of(String restaurantId, int dateYyyymmdd, String mealTimeId, String mealTimeName,
		String name, String course, String courseName, String submenu, String kcal, String photoUrl, String hallNo,
		String menuCourseType) {
		return new WelstoryMenuItem(restaurantId, dateYyyymmdd, mealTimeId, mealTimeName, name, course, courseName,
			submenu, kcal, photoUrl, hallNo, menuCourseType);
	}
}
