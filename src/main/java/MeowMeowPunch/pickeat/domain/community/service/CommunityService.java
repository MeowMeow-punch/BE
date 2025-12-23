package MeowMeowPunch.pickeat.domain.community.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityListResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunitySummaryDto;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityDetailResponse;
import MeowMeowPunch.pickeat.domain.community.dto.request.CommunityLikeRequest;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityDetailResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunityDetailResponse.RelatedPostResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunitySearchResponse;
import MeowMeowPunch.pickeat.domain.community.dto.response.CommunitySearchResponse.SearchPostDetail;
import MeowMeowPunch.pickeat.domain.community.entity.Community;
import MeowMeowPunch.pickeat.domain.community.entity.CommunityLike;
import MeowMeowPunch.pickeat.domain.community.exception.CommunityNotFoundException;
import MeowMeowPunch.pickeat.domain.community.exception.InvalidCategoryException;
import MeowMeowPunch.pickeat.domain.community.exception.InvalidSearchKeywordException;
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
		boolean isAllCategory = "ALL".equalsIgnoreCase(categoryStr);
		CommunityCategory category = null;

		if (!isAllCategory) {
			try {
				category = CommunityCategory.valueOf(categoryStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw InvalidCategoryException.invalidName(categoryStr);
			}
		}
		
		PageRequest pageRequest = PageRequest.of(0, size);

		Slice<Community> communitySlice;
		if (isAllCategory) {
			if (cursorId == null) {
				communitySlice = communityRepository.findAllByOrderByIdDesc(pageRequest);
			} else {
				communitySlice = communityRepository.findByIdLessThanOrderByIdDesc(cursorId, pageRequest);
			}
		} else {
			if (cursorId == null) {
				communitySlice = communityRepository.findByCategoryOrderByIdDesc(category, pageRequest);
			} else {
				communitySlice = communityRepository.findByCategoryAndIdLessThanOrderByIdDesc(category, cursorId, pageRequest);
			}
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

	/**
	 * 커뮤니티 게시글 검색
	 *
	 * @param keyword 검색어 (제목 or 본문 포함)
	 * @return 검색 결과 (게시글 리스트 + 개수)
	 */
	public CommunitySearchResponse searchCommunity(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return CommunitySearchResponse.of(List.of());
		}


		if (keyword.length() < 2) {
			throw new InvalidSearchKeywordException("검색어는 최소 2자 이상이어야 합니다.");
		}

		if (keyword.length() > 50) {
			throw new InvalidSearchKeywordException("검색어는 50자를 초과할 수 없습니다.");
		}

		List<SearchPostDetail> posts = communityRepository.findByTitleContainingOrContentContainingOrderByIdDesc(keyword, keyword)
			.stream()
			.map(SearchPostDetail::from)
			.toList();


		return CommunitySearchResponse.of(posts);
	}

	/**
	 * 커뮤니티 게시글 좋아요 상태 변경 (Atomic Update & 멱등성 보장)
	 *
	 * @param communityId 게시글 ID
	 * @param principal   요청 사용자 정보 (NotNull by Controller)
	 * @param request     좋아요 요청 상태 (true/false)
	 */
	@Transactional
	public void updateLikeStatus(Long communityId, UserPrincipal principal, CommunityLikeRequest request) {
		// 1. 게시글 존재 확인
		if (!communityRepository.existsById(communityId)) {
			throw new CommunityNotFoundException(communityId);
		}

		String userId = principal.getUserId().toString();
		boolean exists = communityLikeRepository.existsByUserIdAndCommunityId(userId, communityId);

		// 2. 멱등성 및 상태 동기화 로직
		if (request.getIsLiked() && !exists) {
			// 좋아요 추가 요청 (현재 좋아요 안 누른 상태) -> Insert & Increase
			communityLikeRepository.save(CommunityLike.builder()
				.userId(userId)
				.communityId(communityId)
				.build());
			communityRepository.increaseLikeCount(communityId);
		} else if (!request.getIsLiked() && exists) {
			// 좋아요 취소 요청 (현재 좋아요 누른 상태) -> Delete & Decrease
			communityLikeRepository.deleteByUserIdAndCommunityId(userId, communityId);
			communityRepository.decreaseLikeCount(communityId);
		}
		// else: 이미 상태가 동일하면 아무 작업도 하지 않음 (멱등성)
	}
}
