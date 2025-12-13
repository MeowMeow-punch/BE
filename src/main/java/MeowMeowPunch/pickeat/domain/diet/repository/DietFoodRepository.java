package MeowMeowPunch.pickeat.domain.diet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.DietFood;
import MeowMeowPunch.pickeat.domain.diet.entity.DietFoodId;

public interface DietFoodRepository extends JpaRepository<DietFood, DietFoodId> {
}
