package MeowMeowPunch.pickeat.global.llm.exception;

import MeowMeowPunch.pickeat.global.error.exception.InternalServerErrorGroupException;

public class LlmParsingException extends InternalServerErrorGroupException {
	public LlmParsingException(String message) {
		super(message, null);
	}
}
