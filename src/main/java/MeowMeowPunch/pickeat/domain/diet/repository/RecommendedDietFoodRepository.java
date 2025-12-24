package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDietFood;
import org.springframework.stereotype.Repository;

/**
 * [Diet][Repository] 추천 식단-음식 매핑 정보 Repository.
 */
@Repository
public interface RecommendedDietFoodRepository extends JpaRepository<RecommendedDietFood, Long> {

	/**
	 * 특정 추천 식단에 포함된 음식 목록 조회.
	 *
	 * @param recommendedDietId 추천 식단 ID
	 * @return 추천 식단-음식 매핑 리스트
	 */
	List<RecommendedDietFood> findAllByRecommendedDietId(Long recommendedDietId);

}
