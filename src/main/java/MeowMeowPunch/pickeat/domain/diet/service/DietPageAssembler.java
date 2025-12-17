package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.diet.dto.DailyCalorieSum;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.request.DietRequest;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.exception.FoodNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietDateException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietFoodQuantityException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietTimeException;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietDetailItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.Nutrients;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.NutritionDetail;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.NutritionInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietStatus;
import MeowMeowPunch.pickeat.global.common.enums.DietType;

// 식단 페이지 공통 계산, 포매팅 헬퍼
public final class DietPageAssembler {

	private DietPageAssembler() {
	}

	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	// TODO: 유저 테이블 생성되면 삭제 예정
	private static final int GOAL_KCAL = 2000;
	private static final int GOAL_CARBS = 280;
	private static final int GOAL_PROTEIN = 120;
	private static final int GOAL_FAT = 70;
	private static final int GOAL_SUGAR = 50;
	private static final int GOAL_DIETARY_FIBER = 25;
	private static final int GOAL_VITAMIN_A = 700;
	private static final int GOAL_VITAMIN_C = 100;
	private static final int GOAL_VITAMIN_D = 10;
	private static final int GOAL_CALCIUM = 700;
	private static final int GOAL_IRON = 14;
	private static final int GOAL_SODIUM = 2000;

	// 필요 시 YYYY-MM-DD 문자열 검증용
	public static LocalDate parseDateOrToday(String raw) {
		if (!StringUtils.hasText(raw)) {
			return LocalDate.now(KOREA_ZONE);
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

		// 오늘 등록 식단 응답 생성
	public static TodayDietInfo toTodayDietInfo(Diet diet, List<String> thumbnailUrls) {
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
			thumbnailUrls
		);
	}

	// 단일 식단 상세 응답 생성
	public static DietInfo toDietInfo(Diet diet, List<DietFood> dietFoods, Map<Long, Food> foodById) {
		List<DietDetailItem> foods = dietFoods.stream()
			.map(df -> toDietFoodItem(foodById.get(df.getFoodId()), df))
			.toList();

		return DietInfo.of(
			diet.getId(),
			diet.getStatus().name(),
			diet.getTime() != null ? diet.getTime().toString() : "",
			diet.getDate().toString(),
			foods
		);
	}

	// 특정 날짜에 등록된 식단들의 부가 영양분 합계를 생성
	public static NutritionInfo buildNutritionInfo(List<Diet> diets) {
		BigDecimal sugar = BigDecimal.ZERO;
		BigDecimal dietaryFiber = BigDecimal.ZERO;
		BigDecimal vitA = BigDecimal.ZERO;
		BigDecimal vitC = BigDecimal.ZERO;
		BigDecimal vitD = BigDecimal.ZERO;
		BigDecimal calcium = BigDecimal.ZERO;
		BigDecimal iron = BigDecimal.ZERO;
		BigDecimal sodium = BigDecimal.ZERO;

		for (Diet diet : diets) {
			sugar = sugar.add(nullSafe(diet.getSugar()));
			dietaryFiber = dietaryFiber.add(nullSafe(diet.getDietaryFiber()));
			vitA = vitA.add(nullSafe(diet.getVitA()));
			vitC = vitC.add(nullSafe(diet.getVitC()));
			vitD = vitD.add(nullSafe(diet.getVitD()));
			calcium = calcium.add(nullSafe(diet.getCalcium()));
			iron = iron.add(nullSafe(diet.getIron()));
			sodium = sodium.add(nullSafe(diet.getSodium()));
		}

		return NutritionInfo.of(
			NutritionDetail.of(toDecimal(sugar), GOAL_SUGAR, "g"),
			NutritionDetail.of(toDecimal(dietaryFiber), GOAL_DIETARY_FIBER, "g"),
			NutritionDetail.of(toDecimal(vitA), GOAL_VITAMIN_A, "ug_RAE"),
			NutritionDetail.of(toDecimal(vitC), GOAL_VITAMIN_C, "mg"),
			NutritionDetail.of(toDecimal(vitD), GOAL_VITAMIN_D, "ug"),
			NutritionDetail.of(toDecimal(calcium), GOAL_CALCIUM, "mg"),
			NutritionDetail.of(toDecimal(iron), GOAL_IRON, "mg"),
			NutritionDetail.of(toDecimal(sodium), GOAL_SODIUM, "mg")
		);
	}

	// 식단별 음식 썸네일 리스트 생성
	public static Map<Long, List<String>> buildThumbnailsByDiet(Map<Long, List<DietFood>> dietFoodsByDietId,
		Map<Long, Food> foodById) {
		return dietFoodsByDietId.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> e.getValue().stream()
					.map(df -> foodById.get(df.getFoodId()))
					.filter(Objects::nonNull)
					.map(Food::getThumbnailUrl)
					.distinct()
					.toList()
			));
	}

	// 추가한 음식들을 하나의 식단으로 집계
	public static DietAggregation aggregateFoods(List<DietRequest.FoodQuantity> foods, Map<Long, Food> foodById) {
		BigDecimal totalKcal = BigDecimal.ZERO;
		BigDecimal totalCarbs = BigDecimal.ZERO;
		BigDecimal totalProtein = BigDecimal.ZERO;
		BigDecimal totalFat = BigDecimal.ZERO;
		BigDecimal totalSugar = BigDecimal.ZERO;
		BigDecimal totalVitA = BigDecimal.ZERO;
		BigDecimal totalVitC = BigDecimal.ZERO;
		BigDecimal totalVitD = BigDecimal.ZERO;
		BigDecimal totalCalcium = BigDecimal.ZERO;
		BigDecimal totalIron = BigDecimal.ZERO;
		BigDecimal totalDietaryFiber = BigDecimal.ZERO;
		BigDecimal totalSodium = BigDecimal.ZERO;

		List<String> names = new ArrayList<>();
		String thumbnailUrl = null;

		for (DietRequest.FoodQuantity item : foods) {
			Food food = foodById.get(item.foodId());
			short quantity = toQuantity(item.quantity());
			BigDecimal multiplier = BigDecimal.valueOf(quantity);

			names.add(food.getName());
			if (thumbnailUrl == null) {
				thumbnailUrl = food.getThumbnailUrl();
			}

			totalKcal = totalKcal.add(nullSafe(food.getKcal()).multiply(multiplier));
			totalCarbs = totalCarbs.add(nullSafe(food.getCarbs()).multiply(multiplier));
			totalProtein = totalProtein.add(nullSafe(food.getProtein()).multiply(multiplier));
			totalFat = totalFat.add(nullSafe(food.getFat()).multiply(multiplier));
			totalSugar = totalSugar.add(nullSafe(food.getSugar()).multiply(multiplier));
			totalVitA = totalVitA.add(nullSafe(food.getVitA()).multiply(multiplier));
			totalVitC = totalVitC.add(nullSafe(food.getVitC()).multiply(multiplier));
			totalVitD = totalVitD.add(nullSafe(food.getVitD()).multiply(multiplier));
			totalCalcium = totalCalcium.add(nullSafe(food.getCalcium()).multiply(multiplier));
			totalIron = totalIron.add(nullSafe(food.getIron()).multiply(multiplier));
			totalDietaryFiber = totalDietaryFiber.add(nullSafe(food.getDietaryFiber()).multiply(multiplier));
			totalSodium = totalSodium.add(nullSafe(food.getSodium()).multiply(multiplier));
		}

		String title = String.join(", ", names);

		return new DietAggregation(
			title,
			thumbnailUrl,
			totalKcal,
			totalCarbs,
			totalProtein,
			totalFat,
			totalSugar,
			totalVitA,
			totalVitC,
			totalVitD,
			totalCalcium,
			totalIron,
			totalDietaryFiber,
			totalSodium
		);
	}

	// quantity 값 검증
	public static short toQuantity(Integer quantity) {
		if (quantity == null || quantity <= 0) {
			throw new InvalidDietFoodQuantityException(quantity == null ? 0 : quantity);
		}
		if (quantity > Short.MAX_VALUE) {
			throw new InvalidDietFoodQuantityException(quantity);
		}
		return quantity.shortValue();
	}

	// 식단 상세 아이템 DTO 생성용
	private static DietDetailItem toDietFoodItem(Food food, DietFood dietFood) {
		FoodItem base = FoodDtoMapper.toFoodItem(food);
		return DietDetailItem.from(base, dietFood.getQuantity());
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

	// 시간, 분을 파싱
	public static LocalTime parseTime(String raw) {
		try {
			return LocalTime.parse(raw, DateTimeFormatter.ofPattern("HH:mm"));
		} catch (DateTimeParseException e) {
			throw new InvalidDietTimeException(raw);
		}
	}

	// 음식 ID가 존재하는지 검증
	public static void validateFoodsExist(List<Long> requestedFoodIds, Map<Long, Food> foodById) {
		requestedFoodIds.stream()
			.filter(id -> !foodById.containsKey(id))
			.findFirst()
			.ifPresent(id -> {
				throw new FoodNotFoundException(id);
			});
	}

	// 소수점 처리: 값이 정수면 소수부 제거, 있으면 한 자리까지 반올림
	private static BigDecimal toDecimal(BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal scaled = value.setScale(1, RoundingMode.HALF_UP);
		BigDecimal stripped = scaled.stripTrailingZeros();
		// stripTrailingZeros 가 0.x 형태에서 scale 음수로 내려가는 경우를 보정
		if (stripped.scale() < 0) {
			return stripped.setScale(0);
		}
		return stripped;
	}

	// 식단 집계용 DTO
	public static record DietAggregation(
		String title,
		String thumbnailUrl,
		BigDecimal kcal,
		BigDecimal carbs,
		BigDecimal protein,
		BigDecimal fat,
		BigDecimal sugar,
		BigDecimal vitA,
		BigDecimal vitC,
		BigDecimal vitD,
		BigDecimal calcium,
		BigDecimal iron,
		BigDecimal dietaryFiber,
		BigDecimal sodium
	) {
	}
}
