package MeowMeowPunch.pickeat.domain.diet.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.global.common.enums.DietType;

public interface RecommendedDietRepository extends JpaRepository<RecommendedDiet, Long> {
	// 오늘 날짜로 DietType(아침, 점심, 저녁, 간식)에 해당하는 데이터가 있는지 조회
	List<RecommendedDiet> findByUserIdAndDateAndDietTypeOrderByCreatedAtDesc(String userId, LocalDate date,
		DietType dietType);
}
