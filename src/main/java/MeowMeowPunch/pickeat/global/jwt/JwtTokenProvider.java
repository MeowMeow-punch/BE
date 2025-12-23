package MeowMeowPunch.pickeat.global.jwt;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * <h2>JwtTokenProvider</h2>
 * <p>
 * JWT(Json Web Token)의 <b>생성(Signing)</b>과 <b>검증(Parsing)</b>을 담당하는 컴포넌트.<br>
 * 대칭키 알고리즘(HS512)을 사용하여 토큰을 서명한다.
 * </p>
 *
 * <pre>
 *    User Entity         Secret Key(HS512)          JWT String
 * ┌──────────────┐      ┌────────────────┐      ┌────────────────┐
 * │  id, role    │ ────►│      SIGN      │ ────►│ eyJhbGci...    │
 * └──────────────┘      └────────────────┘      └────────────────┘
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	/**
	 * Access Token 생성.
	 * <p>
	 * 유효기간이 짧은 인증용 토큰으로, API 요청 시 Header에 담겨 전송된다.
	 * </p>
	 *
	 * @param user 토큰에 식별 정보를 포함시킬 사용자(User)
	 * @return 서명된 JWT 문자열 (Bearer prefix 미포함)
	 */
	public String createAccessToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plusSeconds(jwtProperties.getAccessTokenValidityInSeconds());

		return Jwts.builder()
				.issuer(jwtProperties.getIssuer())
				.subject(String.valueOf(user.getId()))
				.claim("provider", user.getOauthProvider().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(getSigningKey())
				.compact();
	}

	/**
	 * Refresh Token 생성.
	 * <p>
	 * 유효기간이 긴 재발급용 토큰으로, 쿠키에 담겨 전송된다.<br>
	 * 보안을 위해 최소한의 정보(userId)만 포함한다.
	 * </p>
	 *
	 * @param user 토큰 발급 대상 사용자
	 * @return 재발급용 JWT 문자열
	 */
	public String createRefreshToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plusSeconds(jwtProperties.getRefreshTokenValidityInSeconds());

		return Jwts.builder()
				.issuer(jwtProperties.getIssuer())
				.subject(String.valueOf(user.getId()))
				.claim("type", "refresh")
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(getSigningKey())
				.compact();
	}

	/**
	 * 회원가입용 임시 토큰 생성 (Register Token).
	 * <p>
	 * OAuth 로그인 성공 후 회원가입이 필요할 때 발급되는 단기 토큰.<br>
	 * payload에 oauthId와 provider 정보를 포함하며, 서명되어 있어 위변조가 불가능하다.
	 * </p>
	 *
	 * @param oauthId  소셜 플랫폼의 사용자 식별자
	 * @param provider 소셜 플랫폼 이름 (KAKAO, NAVER, GOOGLE)
	 * @return 서명된 JWT 문자열
	 */
	public String createRegisterToken(String oauthId, String provider) {
		Instant now = Instant.now();
		// 회원가입 토큰 유효시간: 10분 (사용자가 정보를 입력할 시간 고려)
		Instant expiry = now.plusSeconds(600);

		return Jwts.builder()
				.issuer(jwtProperties.getIssuer())
				.subject(oauthId)
				.claim("provider", provider)
				.claim("type", "register")
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(getSigningKey())
				.compact();
	}

	/**
	 * JWT 문자열을 파싱하여 Claims(Payload)를 추출.
	 * <p>
	 * 서명이 유효하지 않거나 만료된 경우 {@link InvalidTokenException}을 발생시킨다.
	 * </p>
	 *
	 * @param token 클라이언트가 보낸 JWT (Bearer 제외)
	 * @return Claims 객체 (subject, expiration 등 포함)
	 * @throws InvalidTokenException 토큰이 만료되었거나 손상된 경우
	 */
	public Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (ExpiredJwtException e) {
			throw e; // 만료된 토큰은 Filter에서 처리하기 위해 그대로 던짐
		} catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			throw InvalidTokenException.invalidToken();
		}
	}

	/**
	 * [Signature] Base64 인코딩된 시크릿 키를 HMAC 서명 키로 변환합니다.
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * [Properties] 외부에서 만료 시간 등의 설정값을 활용할 수 있도록 노출합니다.
	 */
	public JwtProperties getJwtProperties() {
		return jwtProperties;
	}
}