package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.Allergy;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
}
