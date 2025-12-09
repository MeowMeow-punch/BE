package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

// 음식 상세 조회 예외
public class FoodNotFoundException extends NotFoundGroupException {
	public FoodNotFoundException(Long foodId) {
		super("해당 ID의 음식 정보를 찾을 수 없습니다. foodId=" + foodId);
	}

	public FoodNotFoundException(String foodCode) {
		super("해당 코드의 음식 정보를 찾을 수 없습니다. foodCode=" + foodCode);
	}
}
