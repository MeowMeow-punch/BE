package MeowMeowPunch.pickeat.global.llm.provider.openai;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

import java.util.List;

import MeowMeowPunch.pickeat.global.llm.dto.LlmRequest;

/**
 * [Global][LLM][OpenAI] 요청 변환기
 */
public final class OpenAiRequestMapper {
	private OpenAiRequestMapper() {
	}

	public static ChatRequest toChatRequest(String model, LlmRequest req) {
		List<Message> messages = List.of(
			new Message("system", req.system()),
			new Message("user", req.user())
		);
		return new ChatRequest(
			model,
			messages,
			req.options().temperature(),
			req.options().maxOutputTokens()
		);
	}
}
