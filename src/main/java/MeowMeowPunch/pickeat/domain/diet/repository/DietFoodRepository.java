package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFoodId;

public interface DietFoodRepository extends JpaRepository<DietFood, DietFoodId> {
	void deleteAllByDietId(Long dietId);

	List<DietFood> findAllByDietId(Long dietId);

	List<DietFood> findAllByDietIdIn(List<Long> dietIds);
}
