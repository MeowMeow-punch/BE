package MeowMeowPunch.pickeat.domain.auth.dto.response;

/**
 * [Auth][DTO] AuthTokenResponse
 *
 * Access/Refresh JWT 페어를 전달하는 응답 레코드.
 * <p>
 * [Structure]
 * 
 * <pre>
 * {
 *   "accessToken": "eyJhbG...",
 *   "refreshToken": "eyJhbG..."
 * }
 * </pre>
 * </p>
 * - 보안 키워드: AccessToken(Authorization 헤더), RefreshToken(HttpOnly 쿠키)
 * - 테스트 포인트: 응답 바디와 쿠키 세팅을 함께 검증 필요.
 */
public record AuthTokenResponse(
		String accessToken,
		String refreshToken) {
	/**
	 * 토큰 문자열을 받아 응답 객체를 생성.
	 *
	 * @param accessToken  액세스 토큰 값
	 * @param refreshToken 리프레시 토큰 값
	 * @return AuthTokenResponse 인스턴스
	 */
	public static AuthTokenResponse of(String accessToken, String refreshToken) {
		return new AuthTokenResponse(accessToken, refreshToken);
	}

	/**
	 * 기존 토큰 값으로부터 새 응답 레코드를 생성.
	 *
	 * @param accessToken  액세스 토큰 값
	 * @param refreshToken 리프레시 토큰 값
	 * @return AuthTokenResponse 인스턴스
	 */
	public static AuthTokenResponse from(String accessToken, String refreshToken) {
		return new AuthTokenResponse(accessToken, refreshToken);
	}
}