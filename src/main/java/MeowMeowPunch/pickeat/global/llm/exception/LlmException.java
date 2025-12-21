package MeowMeowPunch.pickeat.global.llm.exception;

import MeowMeowPunch.pickeat.global.error.exception.InternalServerErrorGroupException;

// LLM 호출 관련 비즈니스 예외
public class LlmException extends InternalServerErrorGroupException {
	public LlmException(String message) {
		super(message, null);
	}

	public LlmException(String message, Throwable cause) {
		super(message, cause);
	}
}
