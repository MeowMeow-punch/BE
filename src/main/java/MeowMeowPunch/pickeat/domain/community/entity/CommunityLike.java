package MeowMeowPunch.pickeat.domain.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [Community][Entity] CommunityLike
 * 사용자-게시글 간의 '좋아요' 상태를 관리하는 매핑 엔티티
 * <p>
 * [Design Key Points]
 * 1. <b>Scalability</b>: M:N 관계를 별도 테이블로 분리하여 관리
 * 2. <b>Integrity</b>: 복합 Unique Index (user_id + community_id)를 통해 중복 좋아요 원천 차단
 * </p>
 * - 테이블: community_likes
 */
@Getter
@Entity
@Table(name = "community_likes", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"user_id", "community_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "like_id")
	private Long id;

	@Column(name = "user_id", length = 36, nullable = false)
	private String userId;

	@Column(name = "community_id", nullable = false)
	private Long communityId;
}
