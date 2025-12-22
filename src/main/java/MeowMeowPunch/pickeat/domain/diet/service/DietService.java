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

import MeowMeowPunch.pickeat.domain.diet.dto.HomeRecommendationResult;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;
import MeowMeowPunch.pickeat.domain.diet.dto.request.DietRequest;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DailyDietResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietDetailResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietRegisterResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.NutritionResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.RestaurantMenuResponse;
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
import MeowMeowPunch.pickeat.domain.diet.repository.AiFeedBackRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.AiFeedBack;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.DietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.Nutrients;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.RestaurantMenuInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.TodayRestaurantMenuInfo;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.FeedBackType;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.welstory.dto.WelstoryMenuItem;
import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;
import MeowMeowPunch.pickeat.welstory.repository.GroupMappingRepository;
import MeowMeowPunch.pickeat.welstory.service.WelstoryMenuService;
import lombok.RequiredArgsConstructor;

/**
 * [Diet][Service] 식단/식당 메뉴 도메인 서비스.
 *
 * - 식단 메인/일자별/상세/영양 조회
 * - 식단 등록·수정·삭제 및 추천 식단 등록
 * - 사내 식당 메뉴 조회
 */
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
	private final GroupMappingRepository groupMappingRepository;
	private final AiFeedBackRepository aiFeedBackRepository;

	// TODO: User 연동 시 제거 (임시 식당명)
	private final String mockGroupName = "전기부산";

	/**
	 * [Home] 오늘 기준 식단 메인 정보 조회
	 *
	 * @param userId 사용자 식별자
	 * @return DietHomeResponse (요약, AI 피드백 - 추천 식단 기반, 추천 식단)
	 */
	public DietHomeResponse getHome(String userId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		Focus focus = Focus.HEALTHY; // TODO: 사용자 설정에서 읽어오는 것으로 변경 예정
		LocalDate todayDate = LocalDate.now(KOREA_ZONE);

		// 오늘 섭취 합계 (쿼리 1회)
		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, todayDate);

		// 식단 추천 계산 트리거 (이미 있으면 재사용) 후 TOP5 후보 반환
		HomeRecommendationResult recommendationResult = dietRecommendationService.recommendTopFoods(userId,
			focus, totals);

		SummaryInfo summaryInfo = buildSummary(totals);

		AiFeedBack aiFeedBack = AiFeedBack.of(
			recommendationResult.reason(),
			LocalDateTime.now(KOREA_ZONE).withNano(0).toString()
		);

		List<RecommendedDietInfo> recommended = recommendationResult.picks().stream()

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

	/**
	 * [Daily] 특정 날짜 식단 페이지 조회
	 *
	 * @param userId  사용자 식별자
	 * @param rawDate 조회 날짜(YYYY-MM-DD, null/빈값이면 오늘)
	 * @return DailyDietResponse (요약, AI 피드백 - 오늘 식단 기반, 오늘 식단, 식당 메뉴)
	 */
	public DailyDietResponse getDaily(String userId, String rawDate) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		LocalDate targetDate = parseDateOrToday(rawDate);

		NutrientTotals totals = dietRecommendationMapper.findTotalsByDate(userId, targetDate);

		SummaryInfo summaryInfo = buildSummary(totals);

		String feedbackContent = aiFeedBackRepository.findByUserIdAndDateAndType(userId, targetDate, FeedBackType.DAILY)
			.map(MeowMeowPunch.pickeat.domain.diet.entity.AiFeedBack::getContent)
			.orElse("AI 피드백은 준비 중입니다.");

		AiFeedBack aiFeedBack = AiFeedBack.of(
			feedbackContent,
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
						thumbnailsByDietId.getOrDefault(diet.getId(), List.of())))
				.toList();

		Map<String, TodayRestaurantMenuInfo> todayRestaurantMenu = buildTodayRestaurantMenu(
				targetDate,
				mockGroupName,
				groupMappingRepository,
				welstoryMenuService);

		return DailyDietResponse.of(
				targetDate.toString(),
				summaryInfo,
				aiFeedBack,
				todayDietInfo,
				todayRestaurantMenu);
	}

	/**
	 * [Detail] 단일 식단 상세 조회
	 *
	 * @param userId 사용자 식별자
	 * @param dietId 식단 ID
	 * @return DietDetailResponse(식단명, 식사 시간대, 시간, 날짜, 수정 가능 여부, 칼로리, 영양분, 음식 데이터)
	 */
	public DietDetailResponse getDetail(String userId, Long dietId) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		Diet diet = dietRepository.findById(dietId)
				.orElseThrow(() -> new DietDetailNotFoundException(dietId));

		List<DietFood> dietFoods = dietFoodRepository.findAllByDietId(diet.getId());

		// WELSTORY 추천 식단일 경우 food 데이터를 보내지 않음
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

	/**
	 * [Nutrition] 특정 날짜 영양 합계 조회
	 *
	 * @param userId  사용자 식별자
	 * @param rawDate 조회 날짜(YYYY-MM-DD, null/빈값이면 오늘)
	 * @return NutritionResponse(8개의 영양분 섭취량)
	 */
	public NutritionResponse getNutrition(String userId, String rawDate) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		LocalDate targetDate = parseDateOrToday(rawDate);
		List<Diet> diets = dietRepository.findAllByUserIdAndDateOrderByTimeAsc(userId, targetDate);

		return NutritionResponse.from(DietPageAssembler.buildNutritionInfo(diets));
	}

	/**
	 * [RestaurantMenu] 특정 날짜 사내 식당 메뉴 조회
	 *
	 * @param rawDate 조회 날짜(YYYY-MM-DD, null/빈값이면 오늘)
	 * @return RestaurantMenuResponse (식사시간대별 메뉴 리스트)
	 */
	public RestaurantMenuResponse getRestaurantMenus(String rawDate) {
		LocalDate targetDate = parseDateOrToday(rawDate);
		GroupMapping mapping = groupMappingRepository.findByGroupName(mockGroupName)
				.orElse(null);
		if (mapping == null) {
			return RestaurantMenuResponse.from(Map.of());
		}

		int dateYyyymmdd = toYyyymmdd(targetDate);
		Map<String, List<RestaurantMenuInfo>> menusBySlot = new LinkedHashMap<>();

		for (DietType slot : List.of(DietType.BREAKFAST, DietType.LUNCH, DietType.DINNER, DietType.SNACK)) {
			String mealTimeId = mealTimeIdForSlot(slot);
			if (mealTimeId == null) {
				continue;
			}
			List<WelstoryMenuItem> menus = welstoryMenuService.getMenus(mapping.getGroupId(), dateYyyymmdd,
					mealTimeId, slot.name());
			if (menus.isEmpty()) {
				menusBySlot.put(slot.name(), List.of());
				continue;
			}
			List<RestaurantMenuInfo> infos = menus.stream()
					.map(menu -> toRestaurantMenuInfo(menu, welstoryMenuService))
					.toList();
			menusBySlot.put(slot.name(), infos);
		}

		return RestaurantMenuResponse.from(menusBySlot);
	}

	/**
	 * [Register] 추천 식단을 내 식단으로 등록
	 *
	 * @param userId           사용자 식별자
	 * @param recommendationId 추천 식단 ID
	 * @return DietRegisterResponse (등록된 식단 ID)
	 */
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
								.quantity((short) link.getQuantity())
								.build())
						.toList();
				dietFoodRepository.saveAll(dietFoods);
			} else if (recommended.getFoodId() != null) {
				dietFoodRepository.save(
						DietFood.builder()
								.dietId(saved.getId())
								.foodId(recommended.getFoodId())
								.quantity((short) 2)
								.build());
			}
		}
		return DietRegisterResponse.from(saved.getId());
	}

	/**
	 * [Create] 사용자 입력 식단 등록
	 *
	 * @param userId  사용자 식별자
	 * @param request 식단 등록 요청
	 */
	@Transactional
	public void create(String userId, DietRequest request) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}

		LocalDate date = parseDateOrToday(request.date());
		LocalTime time = parseTime(request.time());

		DietAggregation aggregation = prepareAggregation(request, foodRepository);

		Diet diet = Diet.createUserInput(
				userId,
				request.mealType(),
				date,
				time,
				aggregation);

		Diet saved = dietRepository.save(diet);

		List<DietFood> dietFoods = buildDietFoods(saved.getId(), request);
		dietFoodRepository.saveAll(dietFoods);

		dietRecommendationService.generateDailyFeedback(userId, date);
	}

	/**
	 * [Update] 사용자 입력 식단 수정
	 *
	 * @param userId  사용자 식별자
	 * @param dietId  식단 ID
	 * @param request 수정 요청
	 */
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

		DietAggregation aggregation = prepareAggregation(request, foodRepository);

		diet.updateUserInput(
				request.mealType(),
				date,
				time,
				aggregation);

		dietFoodRepository.deleteAllByDietId(dietId);
		List<DietFood> dietFoods = buildDietFoods(dietId, request);
		dietFoodRepository.saveAll(dietFoods);

		dietRecommendationService.generateDailyFeedback(userId, date);
	}

	/**
	 * [Delete] 사용자 입력 식단 삭제
	 *
	 * @param userId 사용자 식별자
	 * @param dietId 식단 ID
	 */
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

		dietRecommendationService.generateDailyFeedback(userId, diet.getDate());
	}
}
