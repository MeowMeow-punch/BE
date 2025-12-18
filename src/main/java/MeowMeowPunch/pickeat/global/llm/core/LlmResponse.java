package MeowMeowPunch.pickeat.global.llm.core;

// LLM 응답 모델(원문 + JSON 추출본)
public record LlmResponse(
	String rawText,
	String jsonText
) {
	public static LlmResponse of(String rawText, String jsonText) {
		return new LlmResponse(rawText, jsonText);
	}
}
