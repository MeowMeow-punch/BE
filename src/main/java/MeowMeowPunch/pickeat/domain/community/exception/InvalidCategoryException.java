package MeowMeowPunch.pickeat.domain.community.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

public class InvalidCategoryException extends InvalidGroupException {
	public InvalidCategoryException(String message) {
		super(message);
	}

	public static InvalidCategoryException invalidName(String category) {
		return new InvalidCategoryException("Invalid category: " + category);
	}
}
