package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.exception.MissingDietUserIdException;
import MeowMeowPunch.pickeat.domain.diet.repository.DietFoodRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRecommendationMapper;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.SummaryInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.TodayDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.WeeklyCaloriesInfo;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
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
				mealSlot(LocalTime.now(KOREA_ZONE)).name(),
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
	public void create(String userId, DietCreateRequest request) {
		if (!StringUtils.hasText(userId)) {
			throw new MissingDietUserIdException();
		}
		LocalDate date = parseDateOrToday(request.date());
		LocalTime time = parseTime(request.time());

		List<Long> requestedFoodIds = request.foods().stream()
			.map(DietCreateRequest.FoodQuantity::foodId)
			.toList();

		// 음식 id 검증
		Map<Long, Food> foodById = foodRepository.findAllById(requestedFoodIds).stream()
			.collect(Collectors.toMap(Food::getId, Function.identity()));
		validateFoodsExist(requestedFoodIds, foodById);

		// 추가한 음식들을 하나의 식단으로 집계
		DietAggregation aggregation = aggregateFoods(request.foods(), foodById);

		Diet diet = Diet.createUserInput(
			userId,
			request.mealType(),
			date,
			time,
			aggregation
		);

		Diet saved = dietRepository.save(diet);

		// DietFood 테이블에 quantity 저장
		List<DietFood> dietFoods = request.foods().stream()
			.map(f -> DietFood.builder()
				.dietId(saved.getId())
				.foodId(f.foodId())
				.quantity(toQuantity(f.quantity()))
				.build())
			.toList();
		dietFoodRepository.saveAll(dietFoods);
	}
}
