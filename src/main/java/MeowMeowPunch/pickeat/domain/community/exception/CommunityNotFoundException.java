package MeowMeowPunch.pickeat.domain.community.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

public class CommunityNotFoundException extends NotFoundGroupException {
	public CommunityNotFoundException(Long communityId) {
		super("해당 게시글을 찾을 수 없습니다. (ID: " + communityId + ")");
	}
}
