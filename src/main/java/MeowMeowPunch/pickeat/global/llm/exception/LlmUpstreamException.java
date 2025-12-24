package MeowMeowPunch.pickeat.global.llm.exception;

import MeowMeowPunch.pickeat.global.error.exception.InternalServerErrorGroupException;

public class LlmUpstreamException extends InternalServerErrorGroupException {
	public LlmUpstreamException(String message, Throwable cause) {
		super(message, cause);
	}
}
