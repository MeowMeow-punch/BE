package MeowMeowPunch.pickeat.global.llm.dto;

import MeowMeowPunch.pickeat.global.common.enums.LlmUseCase;

/**
 * [Global][LLM] LLM 요청 모델
 *
 * <pre>
 * (UseCase, SystemPrompt, UserPrompt, Options)
 * </pre>
 */
public record LlmRequest(
	LlmUseCase useCase,
	String system,
	String user,
	LlmRequestOptions options
) {
}
