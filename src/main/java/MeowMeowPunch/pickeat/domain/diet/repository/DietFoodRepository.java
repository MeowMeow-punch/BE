package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFoodId;
import org.springframework.stereotype.Repository;

/**
 * [Diet][Repository] 식단-음식 매핑 정보 Repository.
 */
@Repository
public interface DietFoodRepository extends JpaRepository<DietFood, DietFoodId> {

	/**
	 * 특정 식단(Diet)에 포함된 모든 음식 매핑 삭제.
	 *
	 * @param dietId 식단 ID
	 */
	void deleteAllByDietId(Long dietId);

	/**
	 * 특정 식단(Diet)에 포함된 음식 목록 조회.
	 *
	 * @param dietId 식단 ID
	 * @return 식단-음식 매핑 리스트
	 */
	List<DietFood> findAllByDietId(Long dietId);

	/**
	 * 여러 식단 ID들에 포함된 모든 음식 목록 조회.
	 *
	 * @param dietIds 식단 ID 리스트
	 * @return 식단-음식 매핑 리스트
	 */
	List<DietFood> findAllByDietIdIn(List<Long> dietIds);
}
