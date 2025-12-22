package MeowMeowPunch.pickeat.global.llm.provider.openai;

import java.util.List;

/**
 * [Global][LLM][OpenAI] OpenAI API 요청/응답 모델
 */
public final class OpenAiApiModels {
	private OpenAiApiModels() {
	}

	public record Message(String role, String content) {
	}

	public record ChatRequest(
		String model,
		List<Message> messages,
		Double temperature,
		Integer max_tokens
	) {
	}

	public record ChatResponse(List<Choice> choices) {
		public record Choice(Message message) {
		}
	}
}
