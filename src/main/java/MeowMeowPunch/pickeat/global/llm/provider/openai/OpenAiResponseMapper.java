package MeowMeowPunch.pickeat.global.llm.provider.openai;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

import java.util.Optional;

import MeowMeowPunch.pickeat.global.llm.dto.LlmResponse;
import MeowMeowPunch.pickeat.global.llm.dto.LlmResultParser;

/**
 * [Global][LLM][OpenAI] 응답 변환기
 */
public final class OpenAiResponseMapper {
	private OpenAiResponseMapper() {
	}

	public static LlmResponse toLlmResponse(ChatResponse res) {
		String raw = Optional.ofNullable(res)
			.map(ChatResponse::choices)
			.filter(choices -> !choices.isEmpty())
			.map(choices -> choices.get(0))
			.map(ChatResponse.Choice::message)
			.map(Message::content)
			.orElse("");

		String json = LlmResultParser.extractJsonOrThrow(raw);
		return LlmResponse.of(raw, json);
	}
}
