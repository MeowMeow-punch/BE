package MeowMeowPunch.pickeat.global.llm.exception;

// LLM 호출 타임아웃 예외
public class LlmTimeoutException extends LlmException {
	public LlmTimeoutException(String message, Throwable cause) { super(message, cause); }
}
