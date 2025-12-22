package MeowMeowPunch.pickeat.domain.diet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import org.springframework.stereotype.Repository;

/**
 * [Diet][Repository] 사용자 식단(Diet) 정보 Repository.
 */
@Repository
public interface DietRepository extends JpaRepository<Diet, Long> {

	/**
	 * 특정 날짜의 사용자 식단을 시간순으로 조회.
	 *
	 * @param userId 사용자 식별자
	 * @param date   조회 날짜
	 * @return 해당 날짜의 식단 리스트 (아침->점심->저녁 순)
	 */
    List<Diet> findAllByUserIdAndDateOrderByTimeAsc(String userId, LocalDate date);

    Optional<Diet> findTopByUserIdAndDateLessThanOrderByDateDesc(String userId, LocalDate date);

    boolean existsByUserIdAndDateAndStatus(String userId, LocalDate date, DietType status);
}
