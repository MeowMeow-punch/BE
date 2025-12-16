package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDietFood;

public interface RecommendedDietFoodRepository extends JpaRepository<RecommendedDietFood, Long> {
	List<RecommendedDietFood> findAllByRecommendedDietId(Long recommendedDietId);
}
