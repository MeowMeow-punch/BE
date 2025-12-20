package MeowMeowPunch.pickeat.welstory.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;
import MeowMeowPunch.pickeat.welstory.gateway.WelstoryMenuGateway;
import lombok.RequiredArgsConstructor;

/**
 * [Welstory][Service] 식단/영양 조회 및 추천 변환
 *
 * - 웰스토리 식단/영양 조회
 * - 추천 후보 DTO 변환
 */
@Service
@RequiredArgsConstructor
public class WelstoryMenuService {

	private final WelstoryMenuGateway gateway;
	private static final String DEFAULT_LUNCH_MEAL_TIME_ID = "2";

	/**
	 * 웰스토리 식단 리스트 조회 (원본 포맷).
	 *
	 * @param restaurantId 식당 ID
	 * @param dateYyyymmdd 날짜(YYYYMMDD, null 시 0)
	 * @param mealTimeId   식사 시간대 ID
	 * @param mealTimeName 식사 시간대 이름
	 * @return 메뉴 리스트
	 */
	public List<WelstoryMenuItem> getMenus(String restaurantId, Integer dateYyyymmdd, String mealTimeId,
		String mealTimeName) {
		int targetDate = (dateYyyymmdd != null) ? dateYyyymmdd : 0;
		String timeId = (mealTimeId != null && !mealTimeId.isBlank()) ? mealTimeId : DEFAULT_LUNCH_MEAL_TIME_ID;
		return gateway.getMeals(restaurantId, targetDate, timeId, mealTimeName);
	}

	/**
	 * 웰스토리 식단+영양을 추천 후보 DTO로 변환.
	 *
	 * @param restaurantId 식당 ID
	 * @param dateYyyymmdd 날짜
	 * @param mealTimeId   식사 시간대 ID
	 * @param mealTimeName 식사 시간대 이름
	 * @return 추천 후보 리스트
	 */
	public List<FoodRecommendationCandidate> getRecommendationCandidates(String restaurantId, Integer dateYyyymmdd,
		String mealTimeId, String mealTimeName) {
		int targetDate = (dateYyyymmdd != null) ? dateYyyymmdd : 0;
		String timeId = (mealTimeId != null && !mealTimeId.isBlank()) ? mealTimeId : DEFAULT_LUNCH_MEAL_TIME_ID;

		List<WelstoryMenuItem> menus = gateway.getMeals(restaurantId, targetDate, timeId, mealTimeName);

		if (menus.isEmpty()) {
			return List.of();
		}

		// 여러 코스가 있을 수 있으므로 상위 2개까지 후보 생성
		List<WelstoryMenuItem> targets = menus.stream().limit(2).toList();

		return targets.stream()
			.map(menu -> buildCandidate(menu, restaurantId, targetDate, timeId))
			.collect(Collectors.toList());
	}

	/**
	 * 특정 식단의 영양 정보 조회.
	 *
	 * @param restaurantId 식당 ID
	 * @param dateYyyymmdd 날짜
	 * @param mealTimeId   식사 시간대 ID
	 * @param hallNo       홀 번호
	 * @param menuCourseType 코스 타입
	 * @return 원본 영양 데이터
	 */
	public List<ApiTypes.RawMealMenuData> getNutrients(String restaurantId, int dateYyyymmdd, String mealTimeId,
		String hallNo, String menuCourseType) {
		return gateway.getNutrients(restaurantId, dateYyyymmdd, mealTimeId, hallNo, menuCourseType);
	}

	private FoodRecommendationCandidate buildCandidate(WelstoryMenuItem menu, String restaurantId, int targetDate,
		String timeId) {
		List<ApiTypes.RawMealMenuData> nutrients = List.of();
		if (!isBlank(menu.hallNo()) && !isBlank(menu.menuCourseType())) {
			nutrients = gateway.getNutrients(restaurantId, targetDate, timeId, menu.hallNo(), menu.menuCourseType());
		}

		BigDecimal totalKcal = BigDecimal.ZERO;
		BigDecimal totalCarbs = BigDecimal.ZERO;
		BigDecimal totalProtein = BigDecimal.ZERO;
		BigDecimal totalFat = BigDecimal.ZERO;

		if (!nutrients.isEmpty()) {
			for (ApiTypes.RawMealMenuData n : nutrients) {
				totalKcal = totalKcal.add(toBigDecimal(n.kcal()));
				totalCarbs = totalCarbs.add(toBigDecimal(n.totCho()));
				totalProtein = totalProtein.add(toBigDecimal(n.totProtein()));
				totalFat = totalFat.add(toBigDecimal(n.totFat()));
			}
		} else {
			totalKcal = toBigDecimal(menu.kcal());
		}

		return new FoodRecommendationCandidate(
			null,
			menu.name(),
			menu.photoUrl(),
			totalKcal,
			totalCarbs,
			totalProtein,
			totalFat,
			menu.course(),
			0.0,
			DietSourceType.WELSTORY
		);
	}

	private BigDecimal toBigDecimal(String value) {
		if (value == null || value.isBlank()) {
			return BigDecimal.ZERO;
		}
		try {
			String cleaned = value.replace(",", "").trim();
			if (cleaned.startsWith(".")) {
				cleaned = "0" + cleaned; // ".0" 같이 앞자리가 없는 경우 보정
			}
			return new BigDecimal(cleaned);
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
