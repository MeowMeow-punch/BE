package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.Allergy;

import org.springframework.stereotype.Repository;

/**
 * [User][Repository] 알러지 정보 Repository.
 */
@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {
}
