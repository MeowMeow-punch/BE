package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 잘못된 시간 포맷 예외
public class InvalidDietTimeException extends InvalidGroupException {
	public InvalidDietTimeException(String raw) {
		super("유효한 시간 형식(HH:mm)이 아닙니다. 입력값=" + raw);
	}
}
