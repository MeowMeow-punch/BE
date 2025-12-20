package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietRegisterResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.NutritionResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDietFood;
import MeowMeowPunch.pickeat.domain.diet.exception.DietAccessDeniedException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietDetailNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietFoodNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietNotEditableException;
import MeowMeowPunch.pickeat.domain.diet.exception.DietNotFoundException;
import MeowMeowPunch.pickeat.domain.diet.exception.MissingDietUserIdException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.Nutrients;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayRestaurantMenuInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
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
	private final RecommendedDietRepository recommendedDietRepository;
	private final RecommendedDietFoodRepository recommendedDietFoodRepository;
	private final WelstoryMenuService welstoryMenuService;
	private final RestaurantMappingRepository restaurantMappingRepository;

	// TODO: User 연동 시 제거 (임시 식당명)
	private final String mockRestaurantName = "전기부산";

	// 메인 페이지 조회 (오늘 기준)
	public DietHomeResponse getHome(String userId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		Focus focus = Focus.BALANCE; // TODO: 사용자 설정에서 읽어오는 것으로 변경 예정
		LocalDate todayDate = LocalDate.now(KOREA_ZONE);

		// 오늘 섭취 합계 (쿼리 1회)
		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, todayDate);

		// 식단 추천 계산 트리거 (이미 있으면 재사용) 후 TOP5 후보 반환
		List<FoodRecommendationCandidate> recommendedCandidates = dietRecommendationService.recommendTopFoods(userId,
			focus, totals);

		SummaryInfo summaryInfo = buildSummary(totals);

		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI 피드백은 준비 중입니다.",
			LocalDateTime.now(KOREA_ZONE).withNano(0).toString()
		);

		List<RecommendedDietInfo> recommended = recommendedCandidates.stream()
			.map(c -> RecommendedDietInfo.of(
				c.foodId(), // 여기서는 FoodRecommendationCandidate.foodId에 RecommendedDiet ID가 담겨옴
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

		// 오늘의 식단 - 시간순으로 정렬
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

		Map<String, TodayRestaurantMenuInfo> todayRestaurantMenu = buildTodayRestaurantMenu(targetDate);

		return DailyDietResponse.of(
			targetDate.toString(),
			summaryInfo,
			aiFeedBack,
			todayDietInfo,
			todayRestaurantMenu
		);
	}

	// 식단 상세 조회
	public DietDetailResponse getDetail(String userId, Long dietId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		Diet diet = dietRepository.findById(dietId)
			.orElseThrow(() -> new DietDetailNotFoundException(dietId));

		List<DietFood> dietFoods = dietFoodRepository.findAllByDietId(diet.getId());
		if (dietFoods.isEmpty() && !diet.isEditable()) {
			DietInfo dietInfo = DietPageAssembler.toDietInfo(diet, List.of(), Map.of());
			return DietDetailResponse.from(dietInfo);
		} else if (dietFoods.isEmpty()) {
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

	// 특정 날짜의 상세 영양분 조회
	public NutritionResponse getNutrition(String userId, String rawDate) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		LocalDate targetDate = parseDateOrToday(rawDate);
		List<Diet> diets = dietRepository.findAllByUserIdAndDateOrderByTimeAsc(userId, targetDate);

		return NutritionResponse.from(DietPageAssembler.buildNutritionInfo(diets));
	}

	// 추천 식단을 바로 등록
	@Transactional
	public DietRegisterResponse registerRecommendation(String userId, Long recommendationId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		RecommendedDiet recommended = recommendedDietRepository.findById(recommendationId)
			.orElseThrow(() -> new DietDetailNotFoundException(recommendationId));
		if (!recommended.getUserId().equals(userId)) {
			throw new DietAccessDeniedException(recommendationId);
		}

		DietSourceType sourceType = recommended.getSourceType() != null ? recommended.getSourceType()
			: DietSourceType.FOOD_DB;
		boolean editable = sourceType != DietSourceType.WELSTORY;
		LocalDate date = recommended.getDate();
		LocalTime time = LocalTime.now(KOREA_ZONE);

		Diet diet = Diet.builder()
			.userId(userId)
			.status(recommended.getDietType())
			.sourceType(sourceType)
			.editable(editable)
			.title(recommended.getTitle())
			.date(date)
			.time(time)
			.thumbnailUrl(recommended.getThumbnailUrl())
			.kcal(recommended.getKcal())
			.carbs(recommended.getCarbs())
			.protein(recommended.getProtein())
			.fat(recommended.getFat())
			.sugar(BigDecimal.ZERO)
			.vitA(BigDecimal.ZERO)
			.vitC(BigDecimal.ZERO)
			.vitD(BigDecimal.ZERO)
			.calcium(BigDecimal.ZERO)
			.iron(BigDecimal.ZERO)
			.dietaryFiber(BigDecimal.ZERO)
			.sodium(BigDecimal.ZERO)
			.build();

		Diet saved = dietRepository.save(diet);

		if (editable) {
			List<RecommendedDietFood> links = recommendedDietFoodRepository.findAllByRecommendedDietId(
				recommendationId);
			if (!links.isEmpty()) {
				List<DietFood> dietFoods = links.stream()
					.map(link -> DietFood.builder()
						.dietId(saved.getId())
						.foodId(link.getFoodId())
						.quantity((short)link.getQuantity())
						.build())
					.toList();
				dietFoodRepository.saveAll(dietFoods);
			} else if (recommended.getFoodId() != null) {
				dietFoodRepository.save(
					DietFood.builder()
						.dietId(saved.getId())
						.foodId(recommended.getFoodId())
						.quantity((short)1)
						.build()
				);
			}
		}

		return DietRegisterResponse.of(saved.getId());
	}

	// 식단 등록
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

	// 식단 수정
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
		if (!diet.isEditable()) {
			throw new DietNotEditableException(dietId);
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

	// 식단 삭제
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
		if (!diet.isEditable()) {
			throw new DietNotEditableException(dietId);
		}

		dietFoodRepository.deleteAllByDietId(dietId);
		dietRepository.delete(diet);
	}

	// 식단 추가/수정을 위한 집계 사전 작업
	private DietAggregation prepareAggregation(DietRequest request) {
		List<Long> requestedFoodIds = request.foods().stream()
			.map(DietRequest.FoodQuantity::foodId)
			.toList();

		Map<Long, Food> foodById = foodRepository.findAllById(requestedFoodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(requestedFoodIds, foodById);

		return aggregateFoods(request.foods(), foodById);
	}

	// 식단 추가/수정을 위한 식단-음식 중간 테이블 필드 생성
	private List<DietFood> buildDietFoods(Long dietId, DietRequest request) {
		return request.foods().stream()
			.map(f -> DietFood.builder()
				.dietId(dietId)
				.foodId(f.foodId())
				.quantity(toQuantity(f.quantity()))
				.build())
			.toList();
	}

	// 오늘 시간대에 맞는 웰스토리 식단 목록 조회
	private Map<String, TodayRestaurantMenuInfo> buildTodayRestaurantMenu(LocalDate targetDate) {
		RestaurantMapping mapping = restaurantMappingRepository.findByRestaurantName(mockRestaurantName)
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
			var menus = welstoryMenuService.getMenus(mapping.getRestaurantId(), dateYyyymmdd, mealTimeId,
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
				othersNum
			);
			result.put(slot.name(), info);
		}

		return result;
	}
}
