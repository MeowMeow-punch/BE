package MeowMeowPunch.pickeat.global.llm.provider.openai;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

import MeowMeowPunch.pickeat.global.llm.dto.LlmResponse;
import MeowMeowPunch.pickeat.global.llm.dto.LlmResultParser;

/**
 * [Global][LLM][OpenAI] 응답 변환기
 */
public final class OpenAiResponseMapper {
	private OpenAiResponseMapper() {
	}

	public static LlmResponse toLlmResponse(ChatResponse res) {
		String raw = (res == null || res.choices() == null || res.choices().isEmpty()
			|| res.choices().get(0).message() == null)
			? ""
			: String.valueOf(res.choices().get(0).message().content());

		String json = LlmResultParser.extractJsonOrThrow(raw);
		return LlmResponse.of(raw, json);
	}
}
