package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import MeowMeowPunch.pickeat.domain.diet.dto.FoodRecommendationCandidate;
import MeowMeowPunch.pickeat.domain.diet.dto.NutrientTotals;

@Mapper
public interface DietRecommendationMapper {

	NutrientTotals findTodayTotals(@Param("userId") String userId);

	List<FoodRecommendationCandidate> findTopFoodCandidates(
		@Param("remainingKcal") double remainingKcal,
		@Param("remainingCarbs") double remainingCarbs,
		@Param("remainingProtein") double remainingProtein,
		@Param("remainingFat") double remainingFat,
		@Param("weightKcal") double weightKcal,
		@Param("weightCarbs") double weightCarbs,
		@Param("weightProtein") double weightProtein,
		@Param("weightFat") double weightFat,
		@Param("penaltyOverKcal") double penaltyOverKcal,
		@Param("penaltyOverMacro") double penaltyOverMacro,
		@Param("portionFactor") double portionFactor,
		@Param("limit") int limit
	);
}
