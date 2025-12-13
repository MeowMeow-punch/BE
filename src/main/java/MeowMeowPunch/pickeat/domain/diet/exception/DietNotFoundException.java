package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

// 요청한 식단을 찾을 수 없을 때 사용
public class DietNotFoundException extends NotFoundGroupException {
	public DietNotFoundException(Long dietId) {
		super("존재하지 않는 식단입니다. id=" + dietId);
	}
}
