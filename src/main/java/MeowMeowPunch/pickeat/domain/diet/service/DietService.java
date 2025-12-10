package MeowMeowPunch.pickeat.domain.diet.service;

import static MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.diet.dto.response.AiFeedBack;
import MeowMeowPunch.pickeat.domain.diet.dto.response.DietHomeResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.RecommendedDietRepository;
import MeowMeowPunch.pickeat.global.common.dto.response.RecommendedDietInfo;
import MeowMeowPunch.pickeat.global.common.dto.response.SummaryInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {
	// 성별, 신체 정보에 따라 목표 값 계산하는 로직 만들 예정
	private static final int GOAL_KCAL = 2000;
	private static final int GOAL_CARBS = 280;
	private static final int GOAL_PROTEIN = 120;
	private static final int GOAL_FAT = 70;

	private final DietRepository dietRepository;
	private final RecommendedDietRepository recommendedDietRepository;

	// 홈 페이지 조회
	public DietHomeResponse getHome() {
		LocalDate targetDate = LocalDate.now();

		List<Diet> diets = dietRepository.findAllByDate(targetDate);

		BigDecimal totalKcal = BigDecimal.ZERO;
		BigDecimal totalCarbs = BigDecimal.ZERO;
		BigDecimal totalProtein = BigDecimal.ZERO;
		BigDecimal totalFat = BigDecimal.ZERO;

		for (Diet diet : diets) {
			totalKcal = totalKcal.add(nullSafe(diet.getKcal()));
			totalCarbs = totalCarbs.add(nullSafe(diet.getCarbs()));
			totalProtein = totalProtein.add(nullSafe(diet.getProtein()));
			totalFat = totalFat.add(nullSafe(diet.getFat()));
		}

		int currentKcal = toInt(totalKcal);
		int currentCarbs = toInt(totalCarbs);
		int currentProtein = toInt(totalProtein);
		int currentFat = toInt(totalFat);

		SummaryInfo summaryInfo = SummaryInfo.of(
			SummaryInfo.Calorie.of(currentKcal, GOAL_KCAL),
			SummaryInfo.NutrientInfo.of(currentCarbs, GOAL_CARBS, status(currentCarbs, GOAL_CARBS)),
			SummaryInfo.NutrientInfo.of(currentProtein, GOAL_PROTEIN, status(currentProtein, GOAL_PROTEIN)),
			SummaryInfo.NutrientInfo.of(currentFat, GOAL_FAT, status(currentFat, GOAL_FAT))
		);

		// AI 연결 후 수정할 예정
		AiFeedBack aiFeedBack = AiFeedBack.of(
			"AI 피드백은 준비 중입니다.",
			LocalDateTime.now().withNano(0).toString()
		);

		// AI 연결 후 수정할 예정
		List<RecommendedDietInfo> recommended = recommendedDietRepository.findTop5ByOrderByCreatedAtDesc().stream()
			.map(r -> RecommendedDietInfo.of(
				r.getId(),
				r.getTitle(),
				"LUNCH", // 식사 타입 정보가 없어 임시로 LUNCH 지정
				r.getThumbnailUrl(),
				toInt(r.getKcal())
			))
			.toList();

		return DietHomeResponse.of(summaryInfo, aiFeedBack, recommended);
	}
}
