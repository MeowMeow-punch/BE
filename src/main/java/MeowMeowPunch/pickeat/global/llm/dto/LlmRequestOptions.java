package MeowMeowPunch.pickeat.global.llm.dto;

/**
 * [Global][LLM] 생성 옵션
 *
 * @param temperature     창의성 (0.0 ~ 1.0)
 * @param maxOutputTokens 최대 토큰
 */
public record LlmRequestOptions(
	double temperature,
	int maxOutputTokens
) {
	public static LlmRequestOptions of(double temperature, int maxOutputTokens) {
		return new LlmRequestOptions(temperature, maxOutputTokens);
	}
}
