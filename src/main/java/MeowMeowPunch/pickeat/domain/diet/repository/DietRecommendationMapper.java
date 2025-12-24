package MeowMeowPunch.pickeat.domain.diet.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import MeowMeowPunch.pickeat.domain.diet.dto.DailyCalorieSum;
import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;

@Mapper
public interface DietRecommendationMapper {

	NutrientTotals findTotalsByDate(@Param("userId") String userId, @Param("date") LocalDate date);

	List<FoodRecommendationCandidate> findTopFoodCandidates(
		@Param("remainingKcal") BigDecimal remainingKcal,
		@Param("remainingCarbs") BigDecimal remainingCarbs,
		@Param("remainingProtein") BigDecimal remainingProtein,
		@Param("remainingFat") BigDecimal remainingFat,
		@Param("allowedCategories") List<String> allowedCategories,
		@Param("weightKcal") double weightKcal,
		@Param("weightCarbs") double weightCarbs,
		@Param("weightProtein") double weightProtein,
		@Param("weightFat") double weightFat,
		@Param("penaltyOverKcal") double penaltyOverKcal,
		@Param("penaltyOverMacro") double penaltyOverMacro,
		@Param("kcalTolerance") double kcalTolerance,
		@Param("baseUnit") String baseUnit,
		@Param("limit") int limit,
        @Param("quantity") int quantity
	);
}
