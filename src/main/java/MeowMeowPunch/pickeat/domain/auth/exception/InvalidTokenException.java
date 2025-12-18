package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.AuthGroupException;

/**
 * [Auth][Exception] InvalidTokenException
 * JWT 토큰 서명 불일치, 만료, 구조 오류 시 발생.
 * <p>
 * [HTTP Status]
 * - 401 Unauthorized
 * </p>
 * - 필터 처리: JwtAuthenticationFilter에서 캐치되지 않으면 EntryPoint로 전달.
 */
public class InvalidTokenException extends AuthGroupException {
	public InvalidTokenException(String message) {
		super(message);
	}

	public InvalidTokenException() {
		super("유효하지 않은 토큰입니다.");
	}

	public static InvalidTokenException invalidToken() {
		return new InvalidTokenException("유효하지 않은 토큰입니다.");
	}
}