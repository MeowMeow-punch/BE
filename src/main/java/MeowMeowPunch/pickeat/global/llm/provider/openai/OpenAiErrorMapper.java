package MeowMeowPunch.pickeat.global.llm.provider.openai;

import MeowMeowPunch.pickeat.global.llm.exception.LlmUpstreamException;

/**
 * [Global][LLM][OpenAI] 에러 매퍼
 */
public final class OpenAiErrorMapper {
	private OpenAiErrorMapper() {
	}

	public static RuntimeException map(Throwable t) {
		return new LlmUpstreamException("OpenAI 호출 실패: " + t.getMessage(), t);
	}
}
