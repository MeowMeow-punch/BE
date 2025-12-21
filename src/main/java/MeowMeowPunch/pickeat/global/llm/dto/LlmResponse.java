package MeowMeowPunch.pickeat.global.llm.dto;

/**
 * [Global][LLM] LLM 응답 모델
 */
public record LlmResponse(
	String rawText,
	String jsonText
) {
	public static LlmResponse of(String rawText, String jsonText) {
		return new LlmResponse(rawText, jsonText);
	}
}
