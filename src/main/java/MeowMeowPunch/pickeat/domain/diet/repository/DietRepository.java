package MeowMeowPunch.pickeat.domain.diet.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.Diet;
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

	/**
	 * 특정 기간 동안의 사용자 식단 기록 수 조회.
	 *
	 * @param userId    사용자 식별자
	 * @param startDate 시작 날짜
	 * @param endDate   종료 날짜
	 * @return 해당 기간의 식단 기록 횟수
	 */
	long countByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

	/**
	 * 사용자의 전체 식단 기록 수 조회.
	 * 
	 * @param userId 사용자 식별자
	 * @return 전체 식단 기록 횟수
	 */
	long countByUserId(String userId);

	/**
	 * 사용자의 식단 기록 날짜 목록 조회 (최신순, 중복 제거).
	 * 스트릭(streak) 계산에 사용.
	 * 
	 * @param userId 사용자 식별자
	 * @return 식단 기록 날짜 리스트
	 */
	@org.springframework.data.jpa.repository.Query("SELECT DISTINCT d.date FROM Diet d WHERE d.userId = :userId ORDER BY d.date DESC")
	List<LocalDate> findDistinctDatesByUserId(@org.springframework.data.repository.query.Param("userId") String userId);
}
