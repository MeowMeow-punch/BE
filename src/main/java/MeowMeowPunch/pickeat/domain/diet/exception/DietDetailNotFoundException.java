package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

// 요청한 식단 상세를 찾을 수 없을 때 사용
public class DietDetailNotFoundException extends NotFoundGroupException {
	public DietDetailNotFoundException(Long dietId) {
		super("요청한 식단을 찾을 수 없습니다. dietId=" + dietId);
	}
}
