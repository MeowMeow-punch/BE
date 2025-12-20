package MeowMeowPunch.pickeat.welstory.gateway;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import MeowMeowPunch.pickeat.welstory.WelstoryClient;
import MeowMeowPunch.pickeat.welstory.WelstoryMeal;
import MeowMeowPunch.pickeat.welstory.WelstoryRestaurant;
import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;

// 캐시 없이 Welstory API를 직접 호출하는 게이트웨이 구현체
@Component
public class WelstoryMenuGatewayDirect implements WelstoryMenuGateway {
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final WelstoryClient client;

	public WelstoryMenuGatewayDirect(WelstoryClient client) {
		this.client = client;
	}

	@Override
	public List<WelstoryMenuItem> getMeals(String restaurantId, int dateYyyymmdd, String mealTimeId,
		String mealTimeName) {
		int targetDate = dateYyyymmdd > 0 ? dateYyyymmdd : today();
		WelstoryRestaurant restaurant = client.restaurant(restaurantId, "", "");
		List<ApiTypes.RawMealData> meals = restaurant.listMealsRaw(targetDate, mealTimeId);

		return meals.stream()
			.map(m -> WelstoryMenuItem.of(
				restaurantId,
				targetDate,
				mealTimeId,
				mealTimeName != null ? mealTimeName : m.menuMealTypeTxt(),
				m.menuName(),
				m.courseTxt(),
				firstNonBlank(m.menuCourseName(), m.courseTxt(), m.setMenuName()),
				m.subMenuTxt(),
				m.sumKcal(),
				buildPhoto(m.photoUrl(), m.photoCd()),
				m.hallNo(),
				m.menuCourseType()
			))
			.toList();
	}

	@Override
	public List<ApiTypes.RawMealMenuData> getNutrients(String restaurantId, int dateYyyymmdd, String mealTimeId,
		String hallNo, String menuCourseType) {
		int targetDate = dateYyyymmdd > 0 ? dateYyyymmdd : today();
		WelstoryMeal meal = new WelstoryMeal(client, restaurantId, targetDate, mealTimeId, hallNo, menuCourseType);
		return meal.listNutrientsRaw();
	}

	private int today() {
		return Integer.parseInt(LocalDate.now().format(DATE_FMT));
	}

	private String buildPhoto(String photoUrl, String photoCd) {
		if (photoUrl == null || photoCd == null) {
			return null;
		}
		return photoUrl + photoCd;
	}

	private String firstNonBlank(String... values) {
		for (String v : values) {
			if (v != null && !v.isBlank()) {
				return v.trim();
			}
		}
		return null;
	}
}
