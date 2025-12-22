package MeowMeowPunch.pickeat.domain.diet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import org.springframework.stereotype.Repository;

/**
 * [Diet][Repository] 음식(Food) 데이터 Repository.
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

	/**
	 * 음식 이름으로 정확히 조회.
	 *
	 * @param name 음식명 (Exact Match)
	 * @return 음식 엔티티 (없으면 null)
	 */
	Food findByName(String name);
}
