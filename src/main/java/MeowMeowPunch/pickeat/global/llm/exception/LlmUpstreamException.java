package MeowMeowPunch.pickeat.global.llm.exception;

// 업스트림(LM 제공자) 호출 실패 예외
public class LlmUpstreamException extends LlmException {
	public LlmUpstreamException(String message, Throwable cause) { super(message, cause); }
}
