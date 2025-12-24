package MeowMeowPunch.pickeat.domain.diet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// 특정 날짜별 총 칼로리 합계 DTO (주간 차트 집계용)
public record DailyCalorieSum(
	LocalDate date,
	BigDecimal totalKcal
) {
}
