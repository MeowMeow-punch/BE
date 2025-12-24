package MeowMeowPunch.pickeat.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "oauth.naver")
public class NaverProperties {
	private String clientId;
	private String clientSecret;
	private String redirectUri;
}
