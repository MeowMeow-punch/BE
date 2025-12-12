package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.request.DietCreateRequest;
import MeowMeowPunch.pickeat.domain.diet.dto.response.AiFeedBack;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DailyDietResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietCreateResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.exception.FoodNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietFoodCountException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietFoodQuantityException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietDateException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietTimeException;
import MeowMeowPunch.pickeat.domain.diet.exception.MissingDietUserIdException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.WeeklyCaloriesInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {

	private final DietRecommendationMapper dietRecommendationMapper;
	private final DietRecommendationService dietRecommendationService;
	private final DietRepository dietRepository;
	private final DietFoodRepository dietFoodRepository;
	private final FoodRepository foodRepository;

	// 홈 페이지 조회 (오늘 기준)
	public DietHomeResponse getHome(String userId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		Focus focus = Focus.BALANCE; // TODO: 사용자 설정에서 읽어오는 것으로 변경 예정
		LocalDate todayDate = LocalDate.now();

		// 오늘 섭취 합계 (쿼리 1회)
		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, todayDate);

		// 식단 추천 계산 트리거 (이미 있으면 재사용) 후 TOP5 후보 반환
		List<FoodRecommendationCandidate> recommendedCandidates = dietRecommendationService.recommendTopFoods(userId,
			focus, totals);

		SummaryInfo summaryInfo = buildSummary(totals);

		// TODO: AI 연결 예정
		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI 피드백은 준비 중입니다.",
			LocalDateTime.now().withNano(0).toString()
		);

		List<RecommendedDietInfo> recommended = recommendedCandidates.stream()
			.limit(2)
			.map(c -> RecommendedDietInfo.of(
				c.foodId(),
				c.name(),
				mealSlot(LocalTime.now()).name(),
				c.thumbnailUrl(),
				toInt(c.kcal())
			))
			.toList();

		return DietHomeResponse.of(summaryInfo, aiFeedBack, recommended);
	}

	// 특정 날짜 식단 조회
	public DailyDietResponse getDaily(String userId, String rawDate) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		Focus focus = Focus.BALANCE; // TODO: 사용자 설정에서 읽어오는 것으로 변경 예정
		LocalDate targetDate = parseDateOrToday(rawDate);

		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, targetDate);

		SummaryInfo summaryInfo = buildSummary(totals);

		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI 피드백은 준비 중입니다.",
			targetDate.atStartOfDay().toString()
		);

		List<TodayDietInfo> todayDietInfo = dietRepository.findAllByUserIdAndDateOrderByTimeAsc(userId, targetDate)
			.stream()
			.map(DietPageAssembler::toTodayDietInfo)
			.toList();

		LocalDate today = LocalDate.now();
		LocalDate end = targetDate.isAfter(today) ? today : targetDate;
		LocalDate start = end.minusDays(6);
		var calorieSums = dietRecommendationMapper.findDailyCalories(userId, start, end);
		List<WeeklyCaloriesInfo> weeklyCaloriesInfo = buildWeeklyCalories(calorieSums, start);

		return DailyDietResponse.of(
			targetDate.toString(),
			summaryInfo,
			aiFeedBack,
			todayDietInfo,
			weeklyCaloriesInfo
		);
	}

	@Transactional
	public DietCreateResponse create(String userId, DietCreateRequest request) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		if (request.foods() == null || request.foods().size() < 2) {
			throw new InvalidDietFoodCountException();
		}

		LocalDate date = parseRequiredDate(request.date());
		LocalTime time = parseTime(request.time());

		List<Long> requestedFoodIds = request.foods().stream()
			.map(DietCreateRequest.FoodQuantity::foodId)
			.toList();

		Map<Long, Food> foodById = foodRepository.findAllById(requestedFoodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(requestedFoodIds, foodById);

		DietAggregation aggregation = aggregateFoods(request.foods(), foodById);

		Diet diet = Diet.builder()
			.userId(userId)
			.status(request.mealType())
			.title(aggregation.title())
			.date(date)
			.time(time)
			.thumbnailUrl(aggregation.thumbnailUrl())
			.sourceType(DietSourceType.USERINPUT)
			.kcal(aggregation.kcal())
			.carbs(aggregation.carbs())
			.protein(aggregation.protein())
			.fat(aggregation.fat())
			.sugar(aggregation.sugar())
			.vitA(aggregation.vitA())
			.vitC(aggregation.vitC())
			.vitD(aggregation.vitD())
			.calcium(aggregation.calcium())
			.iron(aggregation.iron())
			.dietaryFiber(aggregation.dietaryFiber())
			.sodium(aggregation.sodium())
			.build();

		Diet saved = dietRepository.save(diet);

		List<DietFood> dietFoods = request.foods().stream()
			.map(f -> DietFood.builder()
				.dietId(saved.getId())
				.foodId(f.foodId())
				.quantity(toQuantity(f.quantity()))
				.build())
			.toList();
		dietFoodRepository.saveAll(dietFoods);

		DietCreateResponse.Nutrition nutrition = DietCreateResponse.Nutrition.of(
			aggregation.kcal(),
			aggregation.carbs(),
			aggregation.protein(),
			aggregation.fat(),
			aggregation.sugar(),
			aggregation.vitA(),
			aggregation.vitC(),
			aggregation.vitD(),
			aggregation.calcium(),
			aggregation.iron(),
			aggregation.dietaryFiber(),
			aggregation.sodium()
		);

		return DietCreateResponse.of(
			saved.getId(),
			aggregation.title(),
			aggregation.thumbnailUrl(),
			date,
			request.mealType(),
			time,
			nutrition,
			request.foods()
		);
	}

	private LocalDate parseRequiredDate(String raw) {
		try {
			return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (DateTimeParseException e) {
			throw new InvalidDietDateException(raw);
		}
	}

	private LocalTime parseTime(String raw) {
		try {
			return LocalTime.parse(raw, DateTimeFormatter.ofPattern("HH:mm"));
		} catch (DateTimeParseException e) {
			throw new InvalidDietTimeException(raw);
		}
	}

	private void validateFoodsExist(List<Long> requestedFoodIds, Map<Long, Food> foodById) {
		requestedFoodIds.stream()
			.filter(id -> !foodById.containsKey(id))
			.findFirst()
			.ifPresent(id -> {
				throw new FoodNotFoundException(id);
			});
	}

	private DietAggregation aggregateFoods(List<DietCreateRequest.FoodQuantity> foods, Map<Long, Food> foodById) {
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

		for (DietCreateRequest.FoodQuantity item : foods) {
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

		String title = String.join(" + ", names);

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

	private short toQuantity(Integer quantity) {
		if (quantity == null || quantity <= 0) {
			throw new InvalidDietFoodQuantityException(quantity == null ? 0 : quantity);
		}
		if (quantity > Short.MAX_VALUE) {
			throw new InvalidDietFoodQuantityException(quantity);
		}
		return quantity.shortValue();
	}

	private record DietAggregation(
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
