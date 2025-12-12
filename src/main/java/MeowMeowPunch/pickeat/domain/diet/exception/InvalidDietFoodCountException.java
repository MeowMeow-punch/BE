package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 식단에 포함된 음식 개수가 유효하지 않을 때 사용
public class InvalidDietFoodCountException extends InvalidGroupException {
	public InvalidDietFoodCountException() {
		super("식단에는 최소 2개 이상의 음식이 필요합니다.");
	}
}
