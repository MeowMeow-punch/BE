package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.request.DietRequest;
import MeowMeowPunch.pickeat.domain.diet.dto.response.AiFeedBack;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DailyDietResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietDetailResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.NutritionResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.exception.DietAccessDeniedException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietDetailNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietFoodNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.MissingDietUserIdException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietDetailItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.Nutrients;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayRestaurantMenuInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.welstory.entity.RestaurantMapping;
import MeowMeowPunch.pickeat.welstory.repository.RestaurantMappingRepository;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {
	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

	private final DietRecommendationMapper dietRecommendationMapper;
	private final DietRecommendationService dietRecommendationService;
	private final DietRepository dietRepository;
	private final DietFoodRepository dietFoodRepository;
	private final FoodRepository foodRepository;
	private final WelstoryMenuService welstoryMenuService;
	private final RestaurantMappingRepository restaurantMappingRepository;

	// TODO: User에서 가져오기
	private final String mockRestaurantName = "전기부산";

	// 메인 ?�이지 조회 (?�늘 기�?)
	public DietHomeResponse getHome(String userId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		Focus focus = Focus.BALANCE; // TODO: ?�용???�정?�서 ?�어?�는 것으�?변�??�정
		LocalDate todayDate = LocalDate.now(KOREA_ZONE);

		// ?�늘 ??�� ?�계 (쿼리 1??
		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, todayDate);

		// ?�단 추천 계산 ?�리�?(?��? ?�으�??�사?? ??TOP5 ?�보 반환
		List<FoodRecommendationCandidate> recommendedCandidates = dietRecommendationService.recommendTopFoods(userId,
			focus, totals);

		SummaryInfo summaryInfo = buildSummary(totals);

		// TODO: AI 피드백 반영
		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI 피드백 중입니다.",
			LocalDateTime.now(KOREA_ZONE).withNano(0).toString()
		);

		List<RecommendedDietInfo> recommended = recommendedCandidates.stream()
			.limit(2)
			.map(c -> RecommendedDietInfo.of(
				c.foodId(), // ?�기?�는 FoodRecommendationCandidate.foodId??RecommendedDiet ID가 ?�겨??
				c.name(),
				mealSlot(LocalTime.now(KOREA_ZONE)).name(),
				toThumbnailList(c.thumbnailUrl()),
				toInt(c.kcal()),
				Nutrients.of(
					toInt(c.carbs()),
					toInt(c.protein()),
					toInt(c.fat())
				)
			))
			.toList();

		return DietHomeResponse.of(summaryInfo, aiFeedBack, recommended);
	}

	// ?�정 ?�짜 ?�단 조회
	public DailyDietResponse getDaily(String userId, String rawDate) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		Focus focus = Focus.BALANCE; // TODO: ?�용???�정?�서 ?�어?�는 것으�?변�??�정
		LocalDate targetDate = parseDateOrToday(rawDate);

		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, targetDate);

		SummaryInfo summaryInfo = buildSummary(totals);

		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI ?�드백�? 준�?중입?�다.",
			targetDate.atStartOfDay().toString()
		);

		// ?�늘???�단 - ?�간?�으�??�렬
		List<Diet> diets = dietRepository.findAllByUserIdAndDateOrderByTimeAsc(userId, targetDate);
		List<Long> dietIds = diets.stream()
			.map(Diet::getId)
			.toList();

		Map<Long, List<DietFood>> dietFoodsByDietId = dietIds.isEmpty()
			? Map.of()
			: dietFoodRepository.findAllByDietIdIn(dietIds).stream()
			.collect(Collectors.groupingBy(DietFood::getDietId));

		List<Long> foodIds = dietFoodsByDietId.values().stream()
			.flatMap(List::stream)
			.map(DietFood::getFoodId)
			.distinct()
			.toList();

		Map<Long, Food> foodById = foodRepository.findAllById(foodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(foodIds, foodById);

		Map<Long, List<String>> thumbnailsByDietId = DietPageAssembler.buildThumbnailsByDiet(dietFoodsByDietId,
			foodById);

		List<TodayDietInfo> todayDietInfo = diets.stream()
			.map(diet -> DietPageAssembler.toTodayDietInfo(
				diet,
				thumbnailsByDietId.getOrDefault(diet.getId(), List.of())
			))
			.toList();

		Map<String, List<TodayRestaurantMenuInfo>> todayRestaurantMenu = buildTodayRestaurantMenu(targetDate);

		return DailyDietResponse.of(
			targetDate.toString(),
			summaryInfo,
			aiFeedBack,
			todayDietInfo,
			todayRestaurantMenu
		);
	}

	// ?�단 ?�세 조회
	public DietDetailResponse getDetail(String userId, Long dietId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		Diet diet = dietRepository.findById(dietId)
			.orElseThrow(() -> new DietDetailNotFoundException(dietId));

		List<DietFood> dietFoods = dietFoodRepository.findAllByDietId(diet.getId());
		if (dietFoods.isEmpty()) {
			throw new DietFoodNotFoundException(diet.getId());
		}

		List<Long> foodIds = dietFoods.stream()
			.map(DietFood::getFoodId)
			.toList();

		Map<Long, Food> foodById = foodRepository.findAllById(foodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(foodIds, foodById);

		DietInfo dietInfo = DietPageAssembler.toDietInfo(diet, dietFoods, foodById);
		return DietDetailResponse.from(dietInfo);
	}

	// ?�정 ?�짜???�세 ?�양�?조회
	public NutritionResponse getNutrition(String userId, String rawDate) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		LocalDate targetDate = parseDateOrToday(rawDate);
		List<Diet> diets = dietRepository.findAllByUserIdAndDateOrderByTimeAsc(userId, targetDate);

		return NutritionResponse.from(DietPageAssembler.buildNutritionInfo(diets));
	}


	// ?�단 ?�록
	@Transactional
	public void create(String userId, DietRequest request) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		LocalDate date = parseDateOrToday(request.date());
		LocalTime time = parseTime(request.time());

		DietAggregation aggregation = prepareAggregation(request);

		Diet diet = Diet.createUserInput(
			userId,
			request.mealType(),
			date,
			time,
			aggregation
		);

		Diet saved = dietRepository.save(diet);

		List<DietFood> dietFoods = buildDietFoods(saved.getId(), request);
		dietFoodRepository.saveAll(dietFoods);
	}

	// ?�단 ?�정
	@Transactional
	public void update(String userId, Long dietId, DietRequest request) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		Diet diet = dietRepository.findById(dietId)
			.orElseThrow(() -> new DietNotFoundException(dietId));

		if (!diet.getUserId().equals(userId)) {
			throw new DietAccessDeniedException(dietId);
		}

		LocalDate date = parseDateOrToday(request.date());
		LocalTime time = parseTime(request.time());

		DietAggregation aggregation = prepareAggregation(request);

		diet.updateUserInput(
			request.mealType(),
			date,
			time,
			aggregation
		);

		dietFoodRepository.deleteAllByDietId(dietId);
		List<DietFood> dietFoods = buildDietFoods(dietId, request);
		dietFoodRepository.saveAll(dietFoods);
	}

	// ?�단 ??��
	@Transactional
	public void delete(String userId, Long dietId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		Diet diet = dietRepository.findById(dietId)
			.orElseThrow(() -> new DietNotFoundException(dietId));

		if (!diet.getUserId().equals(userId)) {
			throw new DietAccessDeniedException(dietId);
		}

		dietFoodRepository.deleteAllByDietId(dietId);
		dietRepository.delete(diet);
	}

	// ?�단 추�?/?�정???�한 집계 ?�전 ?�업
	private DietAggregation prepareAggregation(DietRequest request) {
		List<Long> requestedFoodIds = request.foods().stream()
			.map(DietRequest.FoodQuantity::foodId)
			.toList();

		Map<Long, Food> foodById = foodRepository.findAllById(requestedFoodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(requestedFoodIds, foodById);

		return aggregateFoods(request.foods(), foodById);
	}

	// ?�단 추�?/?�정???�한 ?�단-?�식 중간 ?�이�??�드 ?�성
	private List<DietFood> buildDietFoods(Long dietId, DietRequest request) {
		return request.foods().stream()
			.map(f -> DietFood.builder()
				.dietId(dietId)
				.foodId(f.foodId())
				.quantity(toQuantity(f.quantity()))
				.build())
			.toList();
	}

	// ?�늘 ?�간?�??맞는 ?�스?�리 ?�단 목록 조회
	private Map<String, List<TodayRestaurantMenuInfo>> buildTodayRestaurantMenu(LocalDate targetDate) {
		RestaurantMapping mapping = restaurantMappingRepository.findByRestaurantName(mockRestaurantName)
			.orElse(null);
		if (mapping == null) {
			return Map.of();
		}

		int dateYyyymmdd = targetDate.getYear() * 10000 + targetDate.getMonthValue() * 100 + targetDate.getDayOfMonth();
		Map<String, List<TodayRestaurantMenuInfo>> result = new LinkedHashMap<>();

		for (DietType slot : List.of(DietType.BREAKFAST, DietType.LUNCH, DietType.DINNER)) {
			String mealTimeId = mealTimeIdForSlot(slot);
			if (mealTimeId == null) {
				continue;
			}
			var menus = welstoryMenuService.getMenus(mapping.getRestaurantId(), dateYyyymmdd, mealTimeId,
				slot.name());
			if (menus.isEmpty()) {
				continue;
			}
			List<TodayRestaurantMenuInfo> infos = menus.stream()
				.map(menu -> TodayRestaurantMenuInfo.of(
					menu.name(),
					toInt(DietPageAssembler.toBigDecimal(menu.kcal())),
					DietPageAssembler.buildSubName(menu.name(), menu.submenu())
				))
				.toList();
			result.put(slot.name(), infos);
		}

		return result;
	}
}
