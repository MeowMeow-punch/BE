package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 잘못된 size 예외
public class InvalidDietPageSizeException extends InvalidGroupException {
	public InvalidDietPageSizeException(Integer size, int maxLimit) {
		super("허용되지 않는 페이지 size 입니다. 요청값=" + size + ", 최대 허용값=" + maxLimit);
	}
}
