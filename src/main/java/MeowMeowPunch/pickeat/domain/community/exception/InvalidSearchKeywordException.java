package MeowMeowPunch.pickeat.domain.community.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

public class InvalidSearchKeywordException extends InvalidGroupException {
	public InvalidSearchKeywordException(String message) {
		super(message);
	}
}
