package MeowMeowPunch.pickeat.global.llm.exception;

// LLM 응답 포맷 오류 예외
public class LlmBadResponseException extends LlmException {
	public LlmBadResponseException(String message) { super(message); }
	public LlmBadResponseException(String message, Throwable cause) { super(message, cause); }
}
