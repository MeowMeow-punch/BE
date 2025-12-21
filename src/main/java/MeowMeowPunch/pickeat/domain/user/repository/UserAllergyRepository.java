package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.UserAllergy;

import org.springframework.stereotype.Repository;

/**
 * [User][Repository] 사용자-알러지 매핑 정보 Repository.
 */
@Repository
public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {
}
