package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

// 식단에 연결된 음식이 없을 때 사용
public class DietFoodNotFoundException extends NotFoundGroupException {
	public DietFoodNotFoundException(Long dietId) {
		super("식단에 등록된 음식이 없습니다. dietId=" + dietId);
	}
}
