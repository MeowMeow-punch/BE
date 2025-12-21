package MeowMeowPunch.pickeat.global.llm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
	String provider,     // 예: "openai"
	OpenAi openai,       // Nested Config
	Generation generation,
	long timeoutMs
) {
	public record OpenAi(String apiKey, String model, String baseUrl) {}
	public record Generation(double temperature, int maxOutputTokens) {}
}
