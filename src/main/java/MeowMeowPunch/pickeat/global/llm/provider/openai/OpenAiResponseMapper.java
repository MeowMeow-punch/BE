package MeowMeowPunch.pickeat.global.llm.provider.openai;

import MeowMeowPunch.pickeat.global.llm.core.LlmResponse;
import MeowMeowPunch.pickeat.global.llm.core.LlmResultParser;

import static MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiApiModels.*;

// OpenAI 응답 -> 공통 LlmResponse 변환
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
