package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.UserDisease;

import org.springframework.stereotype.Repository;

/**
 * [User][Repository] 사용자-질환 매핑 정보 Repository.
 */
@Repository
public interface UserDiseaseRepository extends JpaRepository<UserDisease, Long> {
}
