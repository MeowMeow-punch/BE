package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

// 요청한 음식 ID가 존재하지 않을 때 사용
public class FoodNotFoundException extends NotFoundGroupException {
	public FoodNotFoundException(Long foodId) {
		super("존재하지 않는 foodId 입니다. id=" + foodId);
	}
}
