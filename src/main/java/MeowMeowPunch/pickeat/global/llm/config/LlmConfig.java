package MeowMeowPunch.pickeat.global.llm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import MeowMeowPunch.pickeat.global.llm.core.LlmClient;
import MeowMeowPunch.pickeat.global.llm.provider.openai.OpenAiLlmClient;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

	@Bean
	public WebClient llmWebClient(LlmProperties props) {
		return WebClient.builder()
			.baseUrl(props.baseUrl())
			.build();
	}

	@Bean
	public LlmClient llmClient(WebClient llmWebClient, LlmProperties props) {
		// 현재는 OpenAI만 사용
		return new OpenAiLlmClient(llmWebClient, props);
	}
}
