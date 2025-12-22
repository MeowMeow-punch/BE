package MeowMeowPunch.pickeat.welstory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;

import org.springframework.stereotype.Repository;

// 그룹 이름/ID 매핑 조회용 JPA 리포지토리
@Repository
public interface GroupMappingRepository extends JpaRepository<GroupMapping, Long> {

	/**
	 * 그룹 이름으로 정확히 일치하는 그룹 정보 조회.
	 *
	 * @param groupName 그룹명 (Exact Match)
	 * @return 그룹 매핑 정보 (Optional)
	 */
	Optional<GroupMapping> findByGroupName(String groupName);
}
