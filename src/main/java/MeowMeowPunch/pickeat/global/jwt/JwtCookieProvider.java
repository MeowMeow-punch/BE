package MeowMeowPunch.pickeat.global.jwt;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * <h2>JwtCookieProvider</h2>
 * <p>
 * Refresh Token을 <b>HttpOnly Cookie</b>로 안전하게 관리하기 위한 유틸리티 클래스.<br>
 * XSS 공격 방지를 위해 JavaScript 접근을 차단하고, Secure 옵션으로 HTTPS 전송을 강제할 수 있다.
 * </p>
 *
 * <pre>
 *     Server                   Client (Browser)
 *   ┌─────────┐  Set-Cookie   ┌──────────────────┐
 *   │ Refresh │ ────────────► │ Cookie Storage   │
 *   │ Token   │               │ (HttpOnly=true)  │
 *   └─────────┘               └──────────────────┘
 * </pre>
 */
@Component
public class JwtCookieProvider {

	private final JwtProperties jwtProperties;

	public JwtCookieProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	/**
	 * [Set-Cookie] 리프레시 토큰을 안전한 쿠키로 감쌈
	 *
	 * @param refreshToken 새로 발급된 리프레시 토큰
	 * @return ResponseCookie 객체
	 */
	public ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from(jwtProperties.getRefreshCookieName(), refreshToken)
				.httpOnly(true)
				.secure(jwtProperties.isRefreshCookieSecure())
				.sameSite(jwtProperties.getRefreshCookieSameSite())
				.path("/")
				.maxAge(Duration.ofSeconds(jwtProperties.getRefreshTokenValidityInSeconds()))
				.build();
	}

	/**
	 * [Set-Cookie] 쿠키 만료를 위한 빈 값을 생성
	 *
	 * @return 삭제용 ResponseCookie
	 */
	public ResponseCookie deleteRefreshTokenCookie() {
		return ResponseCookie.from(jwtProperties.getRefreshCookieName(), "")
				.httpOnly(true)
				.secure(jwtProperties.isRefreshCookieSecure())
				.sameSite(jwtProperties.getRefreshCookieSameSite())
				.path("/")
				.maxAge(Duration.ZERO)
				.build();
	}

	/**
	 * [Header Utility] ResponseCookie를 Set-Cookie 헤더 문자열로 반환
	 */
	public String asHeader(ResponseCookie cookie) {
		return cookie.toString();
	}
}