package MeowMeowPunch.pickeat.domain.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;

/**
 * [Community][Entity] Community
 * 커뮤니티 게시글 본문 및 메타데이터를 관리하는 핵심 엔티티
 * <p>
 * [Design Key Points]
 * 1. <b>Loose Coupling</b>: User와 직접 매핑하지 않고 userId(String)를 사용하여 도메인 간 결합도를 낮춤
 * 2. <b>Performance</b>:
 *    - content(LOB)와 previewText(VARCHAR)를 분리하여 목록 조회 시 I/O 비용 절감
 *    - likes(count)를 반정규화하여 조인 없이 빠른 정렬/조회 지원
 * </p>
 * - 테이블: community (구 contents)
 * - 동시성: likes 업데이트 시 Race Condition 주의 (추후 비관적 락 도입 고려)
 */
@Getter
@Entity
@Table(name = "community")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Community extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "community_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CommunityCategory category;

	@Column(name = "reading_time", length = 5, nullable = false)
	private String readingTime;

	@Column(length = 30, nullable = false)
	private String title;

	@Column(name = "preview_text", length = 50, nullable = false)
	private String previewText;

	@Column(length = 10, nullable = false)
	private String writer;

	@Column(name = "user_id", length = 36, nullable = false)
	private String userId;

	@Column(nullable = false)
	@Builder.Default
	private int likes = 0;

	@Column(name = "thumbnail_url")
	private String thumbnailUrl;

	@Column(columnDefinition = "TEXT")
	private String content;

	/**
	 * [Business Logic] 좋아요 수 증가
	 * <p>
	 * <b>Concurrency Warning</b>:
	 * 단순 카운트 증가 로직으로, 높은 트래픽 환경에서 Race Condition 발생 가능성 있음.
	 * -> 추후 비즈니스 요구사항에 따라 DB Lock(Pessimistic) 또는 Redis 분산 락 적용 검토 필요.
	 * </p>
	 */
	public void increaseLikeCount() {
		this.likes++;
	}

	public void decreaseLikeCount() {
		if (this.likes > 0) {
			this.likes--;
		}
	}
}
