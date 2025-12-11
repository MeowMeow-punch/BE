package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.global.common.enums.DietStatus;

// 식단 페이지 공통 계산, 포매팅 헬퍼
public final class DietPageAssembler {
	private DietPageAssembler() {
	}

	public static LocalDate parseDateOrToday(String raw) {
		if (!StringUtils.hasText(raw)) {
			return LocalDate.now();
		}
		return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
	}

	public static BigDecimal nullSafe(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	public static int toInt(BigDecimal value) {
		if (value == null) {
			return 0;
		}
		return value.setScale(0, RoundingMode.HALF_UP).intValue();
	}

	public static String status(int current, int goal) {
		if (current < goal) {
			return "LACK";
		}
		if (current > goal) {
			return "OVER";
		}
		return "GOOD";
	}

	public static DietStatus mealSlot(LocalTime now) {
		int hour = now.getHour();
		if (hour >= 4 && hour < 10) {
			return DietStatus.BREAKFAST;
		}
		if (hour >= 10 && hour < 15) {
			return DietStatus.LUNCH;
		}
		if (hour >= 15 && hour < 21) {
			return DietStatus.DINNER;
		}
		return DietStatus.SNACK;
	}
}
