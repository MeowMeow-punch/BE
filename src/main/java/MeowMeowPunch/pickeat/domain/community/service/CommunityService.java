package MeowMeowPunch.pickeat.domain.community.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityListResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunitySummaryDto;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityDetailResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityDetailResponse.RelatedPostResponse;
import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.domain.community.exception.CommunityNotFoundException;
import MeowMeowPunch.pickeat.domain.community.exception.InvalidCategoryException;
import MeowMeowPunch.pickeat.domain.community.repository.CommunityLikeRepository;
import MeowMeowPunch.pickeat.domain.community.repository.CommunityRepository;
import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;
import MeowMeowPunch.pickeat.global.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;

/**
 * [Community][Service] CommunityService
 * 커뮤니티 비즈니스 로직을 담당하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

	private final CommunityRepository communityRepository;
	private final CommunityLikeRepository communityLikeRepository;

	/**
	 * 커뮤니티 게시글 목록을 조회합니다. (Cursor Pagination)
	 *
	 * @param categoryStr 카테고리 문자열 (Case-insensitive)
	 * @param cursorId    마지막 조회한 게시글 ID (첫 조회 시 null)
	 * @param size        페이지 크기
	 * @return 커뮤니티 목록 응답 (게시글 리스트 + 페이징 정보)
	 */
	public CommunityListResponse getCommunityList(String categoryStr, Long cursorId, int size) {
		CommunityCategory category;
		try {
			category = CommunityCategory.valueOf(categoryStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw InvalidCategoryException.invalidName(categoryStr);
		}
		
		PageRequest pageRequest = PageRequest.of(0, size);

		Slice<Community> communitySlice;
		if (cursorId == null) {
			communitySlice = communityRepository.findByCategoryOrderByIdDesc(category, pageRequest);
		} else {
			communitySlice = communityRepository.findByCategoryAndIdLessThanOrderByIdDesc(category, cursorId, pageRequest);
		}

		List<CommunitySummaryDto> posts = communitySlice.getContent().stream()
			.map(CommunitySummaryDto::from)
			.toList();

		String nextCursor = null;
		if (communitySlice.hasNext()) {
			// 다음 페이지가 있으면 마지막 요소의 ID를 커서로 반환
			List<Community> content = communitySlice.getContent();
			if (!content.isEmpty()) {
				nextCursor = String.valueOf(content.get(content.size() - 1).getId());
			}
		}

		return CommunityListResponse.of(posts, nextCursor, communitySlice.hasNext());
	}

	/**
	 * 커뮤니티 게시글 상세 조회
	 *
	 * @param communityId 게시글 ID
	 * @param principal   현재 로그인한 사용자 정보 (Nullable)
	 * @return 게시글 상세 정보 + 연관 게시글
	 */
	public CommunityDetailResponse getCommunityDetail(Long communityId, UserPrincipal principal) {
		Community community = communityRepository.findById(communityId)
			.orElseThrow(() -> new CommunityNotFoundException(communityId));

		boolean isLiked = false;
		if (principal != null) {
			isLiked = communityLikeRepository.existsByUserIdAndCommunityId(
				principal.getUserId().toString(),
				communityId
			);
		}

		List<RelatedPostResponse> relatedPosts = communityRepository.findTop3ByCategoryAndIdNot(
				community.getCategory(), communityId, org.springframework.data.domain.PageRequest.of(0, 3)
			).stream()
			.map(RelatedPostResponse::from)
			.toList();

		// TODO: 조회수 증가 로직 (필요 시 추후 구현)

		return CommunityDetailResponse.of(community, isLiked, relatedPosts);
	}
}
