package MeowMeowPunch.pickeat.welstory.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "welstory")
public record WelstoryProperties(
	String baseUrl,
	Duration timeout,
	String username,
	String password
) {
	public WelstoryProperties {
		if (baseUrl == null || baseUrl.isBlank())
			baseUrl = "https://welplus.welstory.com/";
		if (timeout == null)
			timeout = Duration.ofSeconds(8);
	}
}
