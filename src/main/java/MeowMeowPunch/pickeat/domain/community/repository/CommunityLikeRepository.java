package MeowMeowPunch.pickeat.domain.community.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.community.entity.CommunityLike;

/**
 * 커뮤니티 좋아요 데이터 접근 계층
 * <p>
 * 사용자별 좋아요 여부 확인(exists) 및 토글 시 삭제를 위한 조회를 담당합니다.
 * </p>
 */
public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {
	/**
	 * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
	 *
	 * @param userId      사용자 식별자
	 * @param communityId 게시글 식별자
	 * @return 좋아요 존재 여부
	 */
	boolean existsByUserIdAndCommunityId(String userId, Long communityId);

	/**
	 * 특정 사용자의 특정 게시글 좋아요 엔티티를 조회합니다.
	 * (좋아요 취소 시 삭제 대상을 찾기 위해 사용)
	 *
	 * @param userId      사용자 식별자
	 * @param communityId 게시글 식별자
	 * @return CommunityLike 엔티티 (Optional)
	 */
	Optional<CommunityLike> findByUserIdAndCommunityId(String userId, Long communityId);
}
