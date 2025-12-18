package MeowMeowPunch.pickeat.global.llm.core;

// LLM 생성 옵션
public record LlmRequestOptions(
	double temperature,
	int maxOutputTokens
) {
	public static LlmRequestOptions of(double temperature, int maxOutputTokens) {
		return new LlmRequestOptions(temperature, maxOutputTokens);
	}
}
