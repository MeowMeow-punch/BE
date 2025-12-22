package MeowMeowPunch.pickeat.domain.diet.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.RecommendedDiet;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import org.springframework.stereotype.Repository;

/**
 * [Diet][Repository] 추천 식단 정보 Repository.
 */
@Repository
public interface RecommendedDietRepository extends JpaRepository<RecommendedDiet, Long> {

	/**
	 * 사용자의 특정 날짜/끼니에 대한 추천 식단 이력 조회 (최신순).
	 *
	 * @param userId   사용자 식별자
	 * @param date     날짜
	 * @param dietType 끼니 타입 (아침/점심/저녁/간식)
	 * @return 추천 식단 리스트
	 */
	List<RecommendedDiet> findByUserIdAndDateAndDietTypeOrderByCreatedAtDesc(String userId, LocalDate date,
			DietType dietType);
}
