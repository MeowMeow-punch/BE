package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 잘못된 음식 수량 예외
public class InvalidDietFoodQuantityException extends InvalidGroupException {
	public InvalidDietFoodQuantityException(int quantity) {
		super("quantity는 1 이상이어야 합니다. 입력값=" + quantity);
	}
}
