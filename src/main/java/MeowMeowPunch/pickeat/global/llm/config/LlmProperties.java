package MeowMeowPunch.pickeat.global.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
	String provider,     // 예: "openai"
	String model,        // 예: "gpt-4o-mini"
	String baseUrl,
	String apiKey,
	long timeoutMs,
	Generation generation
) {
	public record Generation(double temperature, int maxOutputTokens) {
	}
}
