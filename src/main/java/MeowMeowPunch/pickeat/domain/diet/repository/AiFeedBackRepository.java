package MeowMeowPunch.pickeat.domain.diet.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.diet.entity.AiFeedBack;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.enums.FeedBackType;

public interface AiFeedBackRepository extends JpaRepository<AiFeedBack, Long> {
	Optional<AiFeedBack> findByUserIdAndDateAndType(String userId, LocalDate date, FeedBackType type);

	Optional<AiFeedBack> findByUserIdAndDateAndTypeAndMealType(String userId, LocalDate date, FeedBackType type,
		DietType mealType);
}
