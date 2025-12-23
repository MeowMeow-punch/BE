package MeowMeowPunch.pickeat.domain.community.repository;

import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	 * 특정 카테고리에 속한 커뮤니티 게시글 목록을 조회합니다 (첫 페이지용).
	 *
	 * @param category 조회할 커뮤니티 카테고리
	 * @param pageable 페이징 정보 (limit)
	 * @return 해당 카테고리의 게시글 Slice 객체
	 */
	Slice<Community> findByCategoryOrderByIdDesc(CommunityCategory category, Pageable pageable);

	/**
	 * 특정 게시글(cursor) 이전의 게시글 목록을 조회합니다 (커서 이후 조회용).
	 *
	 * @param category 조회할 커뮤니티 카테고리
	 * @param id       커서 ID (이 ID보다 작은 게시글 조회)
	 * @param pageable 페이징 정보 (limit)
	 * @return 해당 카테고리의 게시글 Slice 객체
	 */
	Slice<Community> findByCategoryAndIdLessThanOrderByIdDesc(CommunityCategory category, Long id, Pageable pageable);

	/**
	 * 모든 커뮤니티 게시글 목록을 조회합니다 (카테고리 무관, 첫 페이지용).
	 *
	 * @param pageable 페이징 정보 (limit)
	 * @return 전체 게시글 Slice 객체
	 */
	Slice<Community> findAllByOrderByIdDesc(Pageable pageable);

	/**
	 * 특정 게시글(cursor) 이전의 모든 게시글 목록을 조회합니다 (카테고리 무관, 커서 이후 조회용).
	 *
	 * @param id       커서 ID (이 ID보다 작은 게시글 조회)
	 * @param pageable 페이징 정보 (limit)
	 * @return 전체 게시글 Slice 객체
	 */
	Slice<Community> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);

	/**
	 * 특정 카테고리의 최신 게시글 3개를 조회합니다 (본문 제외, 현재 게시글 제외).
	 *
	 * @param category 조회할 커뮤니티 카테고리
	 * @param id       제외할 현재 게시글 식별자
	 * @return 연관 게시글 목록 (Top 3)
	 */
	@Query("SELECT c FROM Community c WHERE c.category = :category AND c.id <> :id ORDER BY c.id DESC")
	List<Community> findTop3ByCategoryAndIdNot(@Param("category") CommunityCategory category, @Param("id") Long id, Pageable pageable);

	/**
	 * 제목 또는 본문에 키워드가 포함된 게시글을 조회합니다 (최신순).
	 *
	 * @param titleKeyword   제목 검색어
	 * @param contentKeyword 본문 검색어
	 * @return 검색된 게시글 목록
	 */
	List<Community> findByTitleContainingOrContentContainingOrderByIdDesc(String titleKeyword, String contentKeyword);

	@Modifying
	@Query("UPDATE Community c SET c.likes = c.likes + 1 WHERE c.id = :id")
	void increaseLikeCount(@Param("id") Long id);

	@Modifying
	@Query("UPDATE Community c SET c.likes = c.likes - 1 WHERE c.id = :id AND c.likes > 0")
	void decreaseLikeCount(@Param("id") Long id);
}
