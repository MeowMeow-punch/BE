package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

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
}
