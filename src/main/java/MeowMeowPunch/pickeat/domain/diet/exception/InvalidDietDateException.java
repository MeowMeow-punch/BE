package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 잘못된 날짜 포맷 예외
public class InvalidDietDateException extends InvalidGroupException {
	public InvalidDietDateException(String raw) {
		super("유효한 날짜 형식(YYYY-MM-DD)이 아닙니다. 입력값=" + raw);
	}
}
