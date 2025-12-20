package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.UserAllergy;

public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {
}
