package MeowMeowPunch.pickeat.global.llm.core;

// LLM 요청 모델
public record LlmRequest(
	LlmUseCase useCase,
	String system,
	String user,
	LlmRequestOptions options
) {
}
