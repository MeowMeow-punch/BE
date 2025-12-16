package MeowMeowPunch.pickeat.welstory.dto;

import java.util.List;

// Welstory API 응답 DTO
public final class ApiTypes {
	private ApiTypes() {
	}

	public record MealTimeData(
		String code,
		String codeNm
	) {
	}

	public record MealListData(
		List<RawMealData> mealList
	) {
	}

	// 식당 - 식사
	public record RawMealData(
		String hallNo,
		String menuName,
		String courseTxt,
		String menuCourseType,
		String setMenuName,
		String subMenuTxt,
		String photoUrl,
		String photoCd,
		String sumKcal,
		String menuMealTypeTxt
	) {
	}

	// 식사 - 영양
	public record RawMealMenuData(
		String menuName,
		String typicalMenu, // "Y" | "N"
		String kcal,
		String totCho,
		String totSugar,
		String totFib,
		String totFat,
		String totProtein
	) {
	}

}
