package MeowMeowPunch.pickeat.domain.diet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.NutritionGoals;
import MeowMeowPunch.pickeat.domain.diet.dto.request.DietRequest;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.exception.DietDuplicateException;
import MeowMeowPunch.pickeat.domain.diet.exception.FoodNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietDateException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietFoodQuantityException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietTimeException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietDetailItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.Nutrients;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.NutritionDetail;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.NutritionInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.RestaurantMenuInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayRestaurantMenuInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietStatus;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.welstory.dto.ApiTypes;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;
import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;
import MeowMeowPunch.pickeat.welstory.repository.GroupMappingRepository;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;

// 식단 페이지 공통 계산, 포매팅 헬퍼

/**
 * [Diet][Assembler] 식단/식당 메뉴 응답 조립 및 포맷 변환 유틸.
 *
 * - 식단/영양 응답 DTO 생성
 * - 웰스토리 메뉴/영양 변환
 * - 날짜/시간 파싱, 수치 변환 헬퍼
 */
public final class DietPageAssembler {

	private DietPageAssembler() {
	}

	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

	// 그룹 이름 반환
	public static String getGroupName(User user, GroupMappingRepository groupMappingRepository) {
		if (user.getGroupId() != null) {
			GroupMapping mapping = groupMappingRepository.findByGroupId(String.valueOf(user.getGroupId()))
				.orElse(null);
			if (mapping != null) {
				return mapping.getGroupName();
			}
		}
		return "";
	}

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
	public static SummaryInfo buildSummary(NutrientTotals totals, NutritionGoals goals) {
		int currentKcal = toInt(nullSafe(totals.totalKcal()));
		int currentCarbs = toInt(nullSafe(totals.totalCarbs()));
		int currentProtein = toInt(nullSafe(totals.totalProtein()));
		int currentFat = toInt(nullSafe(totals.totalFat()));

		int goalKcal = toInt(goals.kcal());
		int goalCarbs = toInt(goals.carbs());
		int goalProtein = toInt(goals.protein());
		int goalFat = toInt(goals.fat());

		return SummaryInfo.of(
			SummaryInfo.Calorie.of(currentKcal, goalKcal),
			SummaryInfo.NutrientInfo.of(currentCarbs, goalCarbs, status(currentCarbs, goalCarbs)),
			SummaryInfo.NutrientInfo.of(currentProtein, goalProtein, status(currentProtein, goalProtein)),
			SummaryInfo.NutrientInfo.of(currentFat, goalFat, status(currentFat, goalFat)));
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
				toInt(nullSafe(diet.getFat()))),
			thumbnailUrls);
	}

	// 단일 식단 상세 응답 생성
    public static DietInfo toDietInfo(Diet diet, List<DietFood> dietFoods, Map<Long, Food> foodById) {
        List<DietDetailItem> foods = dietFoods.stream()
                .map(df -> toDietFoodItem(foodById.get(df.getFoodId()), df))
                .toList();

        // 음식 기반 썸네일 수집
        List<String> thumbnails = dietFoods.stream()
                .map(df -> foodById.get(df.getFoodId()))
                .filter(Objects::nonNull)
                .map(Food::getThumbnailUrl)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        // 폴백: Diet 자체 썸네일 사용
        if (thumbnails.isEmpty()) {
            thumbnails = toThumbnailList(diet.getThumbnailUrl());
        }

        return DietInfo.of(
                diet.getId(),
                diet.getTitle(),
                diet.getStatus().name(),
                diet.getTime() != null ? diet.getTime().toString() : "",
                diet.getDate().toString(),
                diet.isEditable(),
                toInt(nullSafe(diet.getKcal())),
                Nutrients.of(
                        toInt(nullSafe(diet.getCarbs())),
                        toInt(nullSafe(diet.getProtein())),
                        toInt(nullSafe(diet.getFat()))),
                foods,
                thumbnails);
    }

	// 단일 썸네일을 리스트로 래핑
	public static List<String> toThumbnailList(String thumbnailUrl) {
		if (!StringUtils.hasText(thumbnailUrl)) {
			return List.of();
		}
		return List.of(thumbnailUrl);
	}

	// 웰스토리 메뉴 서브명 생성 (메인 메뉴 제외)
	public static String buildSubName(String mainName, String subMenu) {
		if (!StringUtils.hasText(subMenu)) {
			return "";
		}
		String joined = Arrays.stream(subMenu.split(","))
			.map(String::trim)
			.filter(s -> !s.isBlank())
			.filter(s -> !s.equals(mainName))
			.collect(Collectors.joining(", "));
		return joined;
	}

	// 웰스토리 mealTimeId 매핑
	public static String mealTimeIdForSlot(DietType mealSlot) {
		return switch (mealSlot) {
			case BREAKFAST -> "1";
			case LUNCH -> "2";
			case DINNER -> "3";
			case SNACK -> "4";
		};
	}

	// 문자열 수치를 BigDecimal로 안전하게 변환
	public static BigDecimal toBigDecimal(String value) {
		if (value == null || value.isBlank()) {
			return BigDecimal.ZERO;
		}
		try {
			String cleaned = value.replace(",", "").trim();
			if (cleaned.startsWith(".")) {
				cleaned = "0" + cleaned;
			}
			return new BigDecimal(cleaned);
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	// 특정 날짜에 등록된 식단들의 부가 영양분 합계를 생성
	public static NutritionInfo buildNutritionInfo(List<Diet> diets, NutritionGoals goals) {
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
			NutritionDetail.of(toDecimal(sugar), goals.sugar(), "g"),
			NutritionDetail.of(toDecimal(dietaryFiber), goals.dietaryFiber(), "g"),
			NutritionDetail.of(toDecimal(vitA), goals.vitaminA(), "ug_RAE"),
			NutritionDetail.of(toDecimal(vitC), goals.vitaminC(), "mg"),
			NutritionDetail.of(toDecimal(vitD), goals.vitaminD(), "ug"),
			NutritionDetail.of(toDecimal(calcium), goals.calcium(), "mg"),
			NutritionDetail.of(toDecimal(iron), goals.iron(), "mg"),
			NutritionDetail.of(toDecimal(sodium), goals.sodium(), "mg"));
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
					.toList()));
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
			totalSodium);
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
		BigDecimal sodium) {
	}

	public static RestaurantMenuInfo toRestaurantMenuInfo(WelstoryMenuItem menu,
		WelstoryMenuService welstoryMenuService) {
		List<ApiTypes.RawMealMenuData> rawNutrients = List.of();
		if (StringUtils.hasText(menu.hallNo()) && StringUtils.hasText(menu.menuCourseType())) {
			rawNutrients = welstoryMenuService.getNutrients(
				menu.restaurantId(),
				menu.dateYyyymmdd(),
				menu.mealTimeId(),
				menu.hallNo(),
				menu.menuCourseType());
		}

		BigDecimal totalKcal = BigDecimal.ZERO;
		BigDecimal totalCarbs = BigDecimal.ZERO;
		BigDecimal totalProtein = BigDecimal.ZERO;
		BigDecimal totalFat = BigDecimal.ZERO;

		if (!rawNutrients.isEmpty()) {
			for (ApiTypes.RawMealMenuData n : rawNutrients) {
				totalKcal = totalKcal.add(toBigDecimal(n.kcal()));
				totalCarbs = totalCarbs.add(toBigDecimal(n.totCho()));
				totalProtein = totalProtein.add(toBigDecimal(n.totProtein()));
				totalFat = totalFat.add(toBigDecimal(n.totFat()));
			}
		} else {
			totalKcal = toBigDecimal(menu.kcal());
		}

		String restaurantName = menu.courseName() == null ? "" : menu.courseName();
		return RestaurantMenuInfo.of(
			menu.name(),
			restaurantName,
			toInt(totalKcal),
			DietPageAssembler.buildSubName(menu.name(), menu.submenu()),
			Nutrients.of(
				toInt(totalCarbs),
				toInt(totalProtein),
				toInt(totalFat)),
			DietPageAssembler.toThumbnailList(menu.photoUrl()));
	}

	public static int toYyyymmdd(LocalDate date) {
		return date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
	}

	// 중복 식단 검사: SNACK 제외, 동일 (userId, date, mealType) 존재 시 예외
	public static void validateNoDuplicateMeal(
		DietRepository dietRepository,
		String userId,
		LocalDate date,
		DietType mealType) {
		if (mealType != null && mealType != DietType.SNACK) {
			boolean exists = dietRepository.existsByUserIdAndDateAndStatus(userId, date, mealType);
			if (exists) {
				throw new DietDuplicateException();
			}
		}
	}

	// 식단 추가/수정을 위한 집계 사전 작업
	public static DietAggregation prepareAggregation(DietRequest request, FoodRepository foodRepository) {
		List<Long> requestedFoodIds = request.foods().stream()
			.map(DietRequest.FoodQuantity::foodId)
			.toList();

		Map<Long, Food> foodById = foodRepository.findAllById(requestedFoodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(requestedFoodIds, foodById);

		return aggregateFoods(request.foods(), foodById);
	}

	// 식단 추가/수정을 위한 식단-음식 중간 테이블 필드 생성
	public static List<DietFood> buildDietFoods(Long dietId, DietRequest request) {
		return request.foods().stream()
			.map(f -> DietFood.builder()
				.dietId(dietId)
				.foodId(f.foodId())
				.quantity(toQuantity(f.quantity()))
				.build())
			.toList();
	}

	// 오늘 시간대에 맞는 웰스토리 식단 목록 조회
	public static Map<String, TodayRestaurantMenuInfo> buildTodayRestaurantMenu(LocalDate targetDate,
		String groupName, GroupMappingRepository groupMappingRepository, WelstoryMenuService welstoryMenuService) {
		GroupMapping mapping = groupMappingRepository.findByGroupName(groupName)
			.orElse(null);
		if (mapping == null) {
			return Map.of();
		}

		int dateYyyymmdd = targetDate.getYear() * 10000 + targetDate.getMonthValue() * 100 + targetDate.getDayOfMonth();
		Map<String, TodayRestaurantMenuInfo> result = new LinkedHashMap<>();

		for (DietType slot : List.of(DietType.BREAKFAST, DietType.LUNCH, DietType.DINNER)) {
			String mealTimeId = mealTimeIdForSlot(slot);
			if (mealTimeId == null) {
				continue;
			}
			var menus = welstoryMenuService.getMenus(mapping.getGroupId(), dateYyyymmdd, mealTimeId,
				slot.name());
			if (menus.isEmpty()) {
				continue;
			}
			var primaryMenu = menus.getFirst();
			int othersNum = Math.max(0, menus.size() - 1);
			TodayRestaurantMenuInfo info = TodayRestaurantMenuInfo.of(
				primaryMenu.name(),
				toInt(DietPageAssembler.toBigDecimal(primaryMenu.kcal())),
				DietPageAssembler.buildSubName(primaryMenu.name(), primaryMenu.submenu()),
				othersNum);
			result.put(slot.name(), info);
		}

		return result;
	}
}
