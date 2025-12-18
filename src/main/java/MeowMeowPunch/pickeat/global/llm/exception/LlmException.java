package MeowMeowPunch.pickeat.global.llm.exception;

// LLM 관련 기본 예외
public class LlmException extends RuntimeException {
	public LlmException(String message) { super(message); }
	public LlmException(String message, Throwable cause) { super(message, cause); }
}
