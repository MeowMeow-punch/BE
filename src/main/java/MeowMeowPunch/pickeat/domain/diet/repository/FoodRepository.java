package MeowMeowPunch.pickeat.domain.diet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {
}
