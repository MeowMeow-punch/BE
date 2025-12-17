package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;
import java.util.Map;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayRestaurantMenuInfo;

// 특정 날짜 식단 응답 DTO
public record DailyDietResponse(
	String selectedDate,
	SummaryInfo summaryInfo,
	AiFeedBack aiFeedbackInfo,
	List<TodayDietInfo> todayDietInfo,
	Map<String, List<TodayRestaurantMenuInfo>> todayRestaurantMenu
) implements DietResponse {
	public static DailyDietResponse of(
		String selectedDate,
		SummaryInfo summaryInfo,
		AiFeedBack aiFeedbackInfo,
		List<TodayDietInfo> todayDietInfo,
		Map<String, List<TodayRestaurantMenuInfo>> todayRestaurantMenu
	) {
		return new DailyDietResponse(selectedDate, summaryInfo, aiFeedbackInfo, todayDietInfo, todayRestaurantMenu);
	}
}
