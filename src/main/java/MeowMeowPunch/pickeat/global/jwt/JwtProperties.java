package MeowMeowPunch.pickeat.global.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import lombok.Getter;

/**
 * <h2>JwtProperties</h2>
 * <p>
 * JWT 서명 키와 유효성 시간 등을 외부 설정 파일(yml)로부터 로드하는 설정 클래스.
 * </p>
 *
 * <pre>
 *   application.yml (prefix: jwt)
 *        │
 *        ▼
 *   ┌───────────────┐
 *   │ JwtProperties │ (Immutable)
 *   └───────────────┘
 * </pre>
 *
 * <ul>
 * <li><b>secret</b> : HS512 서명에 사용할 Base64 인코딩된 시크릿 키</li>
 * <li><b>issuer</b> : 토큰 발급자 (iss claim)</li>
 * <li><b>access/refresh Validity</b> : 각 토큰의 유효 시간 (초 단위)</li>
 * </ul>
 */
@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

	private final String secret;
	private final String issuer;
	private final long accessTokenValidityInSeconds;
	private final long refreshTokenValidityInSeconds;
	private final String refreshCookieName;
	private final boolean refreshCookieSecure;
	private final String refreshCookieSameSite;

	public JwtProperties(String secret, String issuer, long accessTokenValidityInSeconds,
			long refreshTokenValidityInSeconds, String refreshCookieName,
			boolean refreshCookieSecure, String refreshCookieSameSite) {
		Assert.hasText(secret, "jwt.secret must not be blank");
		Assert.hasText(issuer, "jwt.issuer must not be blank");
		Assert.hasText(refreshCookieName, "jwt.refresh-cookie-name must not be blank");

		this.secret = secret;
		this.issuer = issuer;
		this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
		this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
		this.refreshCookieName = refreshCookieName;
		this.refreshCookieSecure = refreshCookieSecure;
		this.refreshCookieSameSite = refreshCookieSameSite;
	}
}