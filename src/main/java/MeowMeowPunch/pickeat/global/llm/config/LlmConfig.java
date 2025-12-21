package MeowMeowPunch.pickeat.global.llm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import MeowMeowPunch.pickeat.global.llm.dto.LlmClient;
import MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiLlmClient;

/**
 * [Global][LLM] LLM 설정 클래스
 *
 * <pre>
 * [LlmProperties] ──▶ [LlmConfig] ──▶ [WebClient] / [LlmClient]
 * </pre>
 *
 * - WebClient 빈 등록
 * - LlmClient 빈 등록
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

	@Bean
	public WebClient llmWebClient(LlmProperties props) {
		String baseUrl = props.openai() != null ? props.openai().baseUrl() : "";
		return WebClient.builder()
			.baseUrl(baseUrl)
			.build();
	}

	@Bean
	public LlmClient llmClient(WebClient llmWebClient, LlmProperties props) {
		// 현재는 OpenAI만 사용
		return new OpenAiLlmClient(llmWebClient, props);
	}
}
