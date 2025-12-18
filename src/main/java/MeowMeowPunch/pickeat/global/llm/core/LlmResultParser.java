package MeowMeowPunch.pickeat.global.llm.core;

// LLM 응답에서 JSON만 추출하기 위한 최소 파서 (추후 강화 예정)
public final class LlmResultParser {
	private LlmResultParser() {
	}

	public static String extractJsonOrThrow(String text) {
		// TODO: ```json``` 제거, 첫 '{'~마지막 '}' 추출 등 보강
		return text;
	}
}
