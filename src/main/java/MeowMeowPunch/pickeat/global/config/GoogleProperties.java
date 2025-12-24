package MeowMeowPunch.pickeat.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "oauth.google")
public class GoogleProperties {
	private String clientId;
	private String redirectUri;
	private String clientSecret;
}
