package MeowMeowPunch.pickeat.domain.diet.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.Diet;

public interface DietRepository extends JpaRepository<Diet, Long> {
	List<Diet> findAllByUserIdAndDateOrderByTimeAsc(String userId, LocalDate date);
}
