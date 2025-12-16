package MeowMeowPunch.pickeat.welstory.config;

import java.util.UUID;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import MeowMeowPunch.pickeat.welstory.WelstoryClient;
import MeowMeowPunch.pickeat.welstory.http.WelstoryHttpClient;

// 웰스토리 연동에 필요한 Bean(WebClient, HttpClient, Client) 구성
@Configuration
@EnableConfigurationProperties(WelstoryProperties.class)
public class WelstoryConfig {

	// Welstory가 요구하는 디바이스 식별값
	@Bean
	public String welstoryDeviceId() {
		return UUID.randomUUID().toString();
	}

	// Welstory 베이스 URL이 적용된 WebClient
	@Bean
	public WebClient welstoryWebClient(WelstoryProperties props) {
		return WebClient.builder()
			.baseUrl(props.baseUrl())
			.build();
	}

	// 토큰/헤더 관리까지 포함한 HTTP 클라이언트
	@Bean
	public WelstoryHttpClient welstoryHttpClient(WebClient welstoryWebClient,
		String welstoryDeviceId,
		WelstoryProperties props,
		ObjectMapper objectMapper) {
		return new WelstoryHttpClient(welstoryWebClient, welstoryDeviceId, props.timeout(), objectMapper);
	}

	// 최종 Welstory API 클라이언트
	@Bean
	public WelstoryClient welstoryClient(WelstoryHttpClient http, WelstoryProperties props) {
		return new WelstoryClient(http, props.username(), props.password());
	}
}
