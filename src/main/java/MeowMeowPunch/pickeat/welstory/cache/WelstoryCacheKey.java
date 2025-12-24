package MeowMeowPunch.pickeat.welstory.cache;

// Welstory 캐시 키 유틸 (Caffeine/Redis 등 캐시 도입 시 재사용)
public final class WelstoryCacheKey {
	private WelstoryCacheKey() {
	}

	// restaurantId + date + mealTimeId
	public static String mealsKey(String restaurantId, int date, String mealTimeId) {
		return String.join(":",
			normalize(restaurantId),
			String.valueOf(date),
			normalize(mealTimeId)
		);
	}

	// restaurantId + date + mealTimeId + hallNo + menuCourseType
	public static String nutrientsKey(String restaurantId, int date, String mealTimeId, String hallNo,
		String menuCourseType) {
		return String.join(":",
			normalize(restaurantId),
			String.valueOf(date),
			normalize(mealTimeId),
			normalize(hallNo),
			normalize(menuCourseType)
		);
	}

	private static String normalize(String value) {
		if (value == null) {
			return "";
		}
		return value.trim().replaceAll("\\s+", "");
	}
}
