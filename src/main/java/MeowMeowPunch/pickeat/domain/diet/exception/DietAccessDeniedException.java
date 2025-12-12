package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.AccessDeniedGroupException;

// 다른 사용자의 식단에 접근할 때 예외
public class DietAccessDeniedException extends AccessDeniedGroupException {
	public DietAccessDeniedException(Long dietId) {
		super("해당 식단에 접근할 권한이 없습니다. id=" + dietId);
	}
}
