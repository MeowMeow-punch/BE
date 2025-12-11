package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietDateException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.Nutrients;
import MeowMeowPunch.pickeat.global.common.dto.response.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.WeeklyCaloriesInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietStatus;
import MeowMeowPunch.pickeat.global.common.enums.DietType;

// 식단 페이지 공통 계산, 포매팅 헬퍼
public class DietPageAssembler {
	// TODO: 유저 테이블 생성되면 삭제 예정
	private static final int GOAL_KCAL = 2000;
	private static final int GOAL_CARBS = 280;
	private static final int GOAL_PROTEIN = 120;
	private static final int GOAL_FAT = 70;

	// 필요 시 YYYY-MM-DD 문자열 검증용
	public static LocalDate parseDateOrToday(String raw) {
		if (!StringUtils.hasText(raw)) {
			return LocalDate.now();
		}
		try {
			return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (DateTimeParseException e) {
			throw new InvalidDietDateException(raw);
		}
	}

	// 목표 대비 현재 상태 반환
	public static DietStatus status(int current, int goal) {
		if (current < goal) {
			return DietStatus.LACK;
		}
		if (current > goal) {
			return DietStatus.OVER;
		}
		return DietStatus.GOOD;
	}

	// 시간대별 DietType 반환
	public static DietType mealSlot(LocalTime now) {
		int hour = now.getHour();
		if (hour >= 4 && hour < 10) {
			return DietType.BREAKFAST;
		}
		if (hour >= 10 && hour < 15) {
			return DietType.LUNCH;
		}
		if (hour >= 15 && hour < 21) {
			return DietType.DINNER;
		}
		return DietType.SNACK;
	}

	// SummaryInfo 응답 생성
	public static SummaryInfo buildSummary(NutrientTotals totals) {
		int currentKcal = toInt(nullSafe(totals.totalKcal()));
		int currentCarbs = toInt(nullSafe(totals.totalCarbs()));
		int currentProtein = toInt(nullSafe(totals.totalProtein()));
		int currentFat = toInt(nullSafe(totals.totalFat()));

		return SummaryInfo.of(
			SummaryInfo.Calorie.of(currentKcal, GOAL_KCAL),
			SummaryInfo.NutrientInfo.of(currentCarbs, GOAL_CARBS, status(currentCarbs, GOAL_CARBS)),
			SummaryInfo.NutrientInfo.of(currentProtein, GOAL_PROTEIN, status(currentProtein, GOAL_PROTEIN)),
			SummaryInfo.NutrientInfo.of(currentFat, GOAL_FAT, status(currentFat, GOAL_FAT))
		);
	}

	// 주간 칼로리 응답 생성 (오른쪽 끝 막대가 오늘 기준)
	public static List<WeeklyCaloriesInfo> buildWeeklyCalories(DietRepository dietRepository, String userId,
		LocalDate targetDate) {
		LocalDate today = LocalDate.now();
		LocalDate end = targetDate.isAfter(today) ? today : targetDate;
		LocalDate start = end.minusDays(6);

		// 7일간 날짜별로 칼로리 합산
		Map<LocalDate, Integer> calorieByDate = dietRepository.findAllByUserIdAndDateBetweenOrderByDateAsc(userId,
				start,
				end)
			.stream()
			.collect(Collectors.toMap(
				Diet::getDate,
				d -> toInt(nullSafe(d.getKcal())),
				Integer::sum
			));

		// 해당 날짜에 칼로리 값이 없으면 0으로 처리
		List<WeeklyCaloriesInfo> result = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			LocalDate date = start.plusDays(i);
			int kcal = calorieByDate.getOrDefault(date, 0);
			String dayKey = date.getDayOfWeek().name().substring(0, 3);
			result.add(WeeklyCaloriesInfo.of(dayKey, kcal));
		}
		return result;
	}

	// 오늘 등록 식단 응답 생성
	public static TodayDietInfo toTodayDietInfo(Diet diet) {
		return TodayDietInfo.of(
			diet.getId(),
			diet.getTitle(),
			diet.getStatus().name(),
			toInt(nullSafe(diet.getKcal())),
			diet.getTime() != null ? diet.getTime().toString() : "",
			Nutrients.of(
				toInt(nullSafe(diet.getCarbs())),
				toInt(nullSafe(diet.getProtein())),
				toInt(nullSafe(diet.getFat()))
			),
			diet.getThumbnailUrl()
		);
	}

	// null -> 0 변환용
	public static BigDecimal nullSafe(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	// Decimal -> int 변환용
	public static int toInt(BigDecimal value) {
		if (value == null) {
			return 0;
		}
		return value.setScale(0, RoundingMode.HALF_UP).intValue();
	}
}
