package MeowMeowPunch.pickeat.global.llm.provider.openai;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

import java.util.List;

import MeowMeowPunch.pickeat.global.llm.core.LlmRequest;

// LlmRequest -> OpenAI ChatRequest 변환
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
