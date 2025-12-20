package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.UserDisease;

public interface UserDiseaseRepository extends JpaRepository<UserDisease, Long> {
}
