package MeowMeowPunch.pickeat.domain.community.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import MeowMeowPunch.pickeat.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;
import MeowMeowPunch.pickeat.domain.community.entity.CommunityLike;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class CommunityRepositoryTest {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private CommunityLikeRepository communityLikeRepository;

	@Test
	@DisplayName("커뮤니티 글 저장 및 조회 테스트")
	void saveAndFindCommunity() {
		// given
		Community community = Community.builder()
			.category(CommunityCategory.DIET)
			.readingTime("5min")
			.title("Test Title")
			.previewText("Preview")
			.writer("tester")
			.userId("user-uuid-123")
			.content("Test Content")
			.thumbnailUrl("http://image.url")
			.build();

		// when
		Community saved = communityRepository.save(community);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getTitle()).isEqualTo("Test Title");
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("카테고리별 목록 조회 테스트")
	void findByCategory() {
		// given
		Community c1 = Community.builder()
			.category(CommunityCategory.EXERCISE)
			.readingTime("3min")
			.title("Exercise 1")
			.previewText("Prev 1")
			.writer("w1")
			.userId("u1")
			.content("Content 1")
			.build();

		Community c2 = Community.builder()
			.category(CommunityCategory.EXERCISE)
			.readingTime("4min")
			.title("Exercise 2")
			.previewText("Prev 2")
			.writer("w2")
			.userId("u2")
			.content("Content 2")
			.build();

		Community c3 = Community.builder()
			.category(CommunityCategory.DIET)
			.readingTime("5min")
			.title("Diet 1")
			.previewText("Prev 3")
			.writer("w3")
			.userId("u3")
			.content("Content 3")
			.build();

		communityRepository.save(c1);
		communityRepository.save(c2);
		communityRepository.save(c3);

		// when
		Slice<Community> result = communityRepository.findByCategoryOrderByIdDesc(
			CommunityCategory.EXERCISE,
			PageRequest.of(0, 10)
		);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent()).extracting("title")
			.containsExactlyInAnyOrder("Exercise 1", "Exercise 2");
	}

	@Test
	@DisplayName("좋아요 중복 등록 방지 Constraint 테스트")
	void uniqueLikeConstraint() {
		// given
		CommunityLike like1 = CommunityLike.builder()
			.userId("user1")
			.communityId(1L)
			.build();
		
		CommunityLike like2 = CommunityLike.builder()
			.userId("user1")
			.communityId(1L)
			.build();

		communityLikeRepository.save(like1);

		// when & then
		// 같은 user + communityId 조합으로 저장 시 예외 발생 검증
		assertThatThrownBy(() -> communityLikeRepository.saveAndFlush(like2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}
