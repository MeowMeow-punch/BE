package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.dto.response.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.WeeklyCaloriesInfo;

public record DailyDietResponse(
	String selectedDate,
	SummaryInfo summaryInfo,
	AiFeedBack aiFeedbackInfo,
	List<TodayDietInfo> todayDietInfo,
	List<WeeklyCaloriesInfo> weeklyCaloriesInfo
) implements DietResponse {
	public static DailyDietResponse of(
		String selectedDate,
		SummaryInfo summaryInfo,
		AiFeedBack aiFeedbackInfo,
		List<TodayDietInfo> todayDietInfo,
		List<WeeklyCaloriesInfo> weeklyCaloriesInfo
	) {
		return new DailyDietResponse(selectedDate, summaryInfo, aiFeedbackInfo, todayDietInfo, weeklyCaloriesInfo);
	}
}
