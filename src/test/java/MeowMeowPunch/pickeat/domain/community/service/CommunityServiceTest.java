package MeowMeowPunch.pickeat.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityListResponse;
import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.domain.community.exception.InvalidCategoryException;
import MeowMeowPunch.pickeat.domain.community.repository.CommunityRepository;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

	@InjectMocks
	private CommunityService communityService;

	@Mock
	private CommunityRepository communityRepository;

	@Test
	@DisplayName("첫 페이지 조회 시 커서가 없으면 최신순 조회")
	void getFirstPage() {
		// given
		Community c1 = createCommunity(2L, "Title 2");
		Community c2 = createCommunity(1L, "Title 1");
		Slice<Community> slice = new SliceImpl<>(List.of(c1, c2), PageRequest.of(0, 5), true);

		given(communityRepository.findByCategoryOrderByIdDesc(eq(CommunityCategory.DIET), any()))
			.willReturn(slice);

		// when
		CommunityListResponse response = communityService.getCommunityList("DIET", null, 5);

		// then
		assertThat(response.posts()).hasSize(2);
		assertThat(response.posts().get(0).title()).isEqualTo("Title 2");
		assertThat(response.pageInfo().nextCursor()).isEqualTo("1"); // 마지막 아이템 ID
		assertThat(response.pageInfo().hasNext()).isTrue();
	}

	@Test
	@DisplayName("커서 ID가 주어지면 해당 ID 이후의 데이터 조회")
	void getNextPage() {
		// given
		Community c1 = createCommunity(10L, "Title 10");
		Slice<Community> slice = new SliceImpl<>(List.of(c1), PageRequest.of(0, 5), true);

		given(communityRepository.findByCategoryAndIdLessThanOrderByIdDesc(eq(CommunityCategory.DIET), eq(11L), any()))
			.willReturn(slice);

		// when
		CommunityListResponse response = communityService.getCommunityList("DIET", 11L, 5);

		// then
		assertThat(response.posts()).hasSize(1);
		assertThat(response.pageInfo().nextCursor()).isEqualTo("10");
		assertThat(response.pageInfo().hasNext()).isTrue();
	}

	@Test
	@DisplayName("유효하지 않은 카테고리 입력 시 예외 발생")
	void invalidCategory() {
		// when & then
		assertThatThrownBy(() -> communityService.getCommunityList("INVALID", null, 5))
			.isInstanceOf(InvalidCategoryException.class)
			.hasMessageContaining("Invalid category: INVALID");
	}

	private Community createCommunity(Long id, String title) {
		return Community.builder()
			.id(id)
			.category(CommunityCategory.DIET)
			.title(title)
			.content("Content")
			.previewText("Preview")
			.userId("user1")
			.readingTime("1min")
			.thumbnailUrl("url")
			.build();
	}
}
