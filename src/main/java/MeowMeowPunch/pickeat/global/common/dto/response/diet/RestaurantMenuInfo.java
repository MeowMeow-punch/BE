package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.List;

// 사내 식당 메뉴 단일 항목 DTO
public record RestaurantMenuInfo(
	String name,
	String restaurantName,
	int calorie,
	String subName,
	Nutrients nutrients,
	List<String> thumbnailUrls
) {
	public static RestaurantMenuInfo of(String name, String restaurantName, int calorie, String subName,
		Nutrients nutrients, List<String> thumbnailUrls) {
		return new RestaurantMenuInfo(name, restaurantName, calorie, subName, nutrients, thumbnailUrls);
	}
}
