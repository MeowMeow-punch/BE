package MeowMeowPunch.pickeat.welstory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;

// 그룹 이름/ID 매핑 조회용 JPA 리포지토리
public interface GroupMappingRepository extends JpaRepository<GroupMapping, Long> {
	Optional<GroupMapping> findByGroupName(String groupName);
}
