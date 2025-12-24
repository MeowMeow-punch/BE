package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 잘못된 cursor 예외
public class InvalidDietCursorException extends InvalidGroupException {
	public InvalidDietCursorException(String cursor) {
		super("유효하지 않은 cursor 값입니다. cursor=" + cursor);
	}
}
