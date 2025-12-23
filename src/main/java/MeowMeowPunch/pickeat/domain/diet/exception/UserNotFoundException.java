package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 사용자 ID가 없는 예외
public class UserNotFoundException extends InvalidGroupException {
	public UserNotFoundException() {
		super("userId가 존재하지 않습니다.");
	}
}
