package MeowMeowPunch.pickeat.domain.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;

/**
 * 커뮤니티 게시글 데이터 접근 계층
 * <p>
 * 단순 CRUD 및 카테고리별 조회 기능을 제공합니다.
 * </p>
 */
public interface CommunityRepository extends JpaRepository<Community, Long> {
	/**
	 * 특정 카테고리에 속한 커뮤니티 게시글 목록을 조회합니다.
	 *
	 * @param category 조회할 커뮤니티 카테고리
	 * @param pageable 페이징 정보 (page, size, sort)
	 * @return 해당 카테고리의 게시글 페이지 객체
	 */
	Page<Community> findByCategory(CommunityCategory category, Pageable pageable);
}
