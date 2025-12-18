package MeowMeowPunch.pickeat.global.llm.provider.openai;

import MeowMeowPunch.pickeat.global.llm.exception.LlmUpstreamException;

// OpenAI 예외를 도메인 예외로 변환
public final class OpenAiErrorMapper {
	private OpenAiErrorMapper() {
	}

	public static RuntimeException map(Throwable t) {
		return new LlmUpstreamException("OpenAI 호출 실패: " + t.getMessage(), t);
	}
}
