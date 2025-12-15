package MeowMeowPunch.pickeat.welstory.config;

import java.util.UUID;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import MeowMeowPunch.pickeat.welstory.WelstoryClient;
import MeowMeowPunch.pickeat.welstory.http.WelstoryHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(WelstoryProperties.class)
public class WelstoryConfig {

	@Bean
	public String welstoryDeviceId() {
		return UUID.randomUUID().toString();
	}

	@Bean
	public WebClient welstoryWebClient(WelstoryProperties props) {
		return WebClient.builder()
			.baseUrl(props.baseUrl())
			.build();
	}

	@Bean
	public WelstoryHttpClient welstoryHttpClient(WebClient welstoryWebClient,
		String welstoryDeviceId,
		WelstoryProperties props,
		ObjectMapper objectMapper) {
		return new WelstoryHttpClient(welstoryWebClient, welstoryDeviceId, props.timeout(), objectMapper);
	}

	@Bean
	public WelstoryClient welstoryClient(WelstoryHttpClient http, WelstoryProperties props) {
		return new WelstoryClient(http, props.username(), props.password());
	}
}
