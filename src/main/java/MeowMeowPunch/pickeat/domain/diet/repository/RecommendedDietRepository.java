package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;

public interface RecommendedDietRepository extends JpaRepository<RecommendedDiet, Long> {
	List<RecommendedDiet> findTop5ByOrderByCreatedAtDesc();
}
