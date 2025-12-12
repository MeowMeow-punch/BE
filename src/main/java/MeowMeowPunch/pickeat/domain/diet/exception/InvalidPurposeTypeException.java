package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

// 지원하지 않는 목적 타입(DIET/BULK_UP/BALANCE) 요청 시 예외
public class InvalidPurposeTypeException extends InvalidGroupException {
	public InvalidPurposeTypeException(String raw) {
		super("유효하지 않은 목적 타입입니다. 요청값=" + raw + " (지원: DIET, BULK_UP, BALANCE)");
	}
}
