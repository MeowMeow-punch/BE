package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 사용자 ID가 없는 예외
public class MissingDietUserIdException extends InvalidGroupException {
	public MissingDietUserIdException() {
		super("사용자 식별자(userId)가 필요합니다.");
	}
}
