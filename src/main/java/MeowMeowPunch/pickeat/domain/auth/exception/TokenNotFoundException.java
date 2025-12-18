package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.AuthGroupException;

/**
 * [Auth][Exception] TokenNotFoundException
 * 로그아웃 시도 시 DB/Redis에 해당 사용자의 리프레시 토큰이 없을 때 발생.
 * <p>
 * [HTTP Status]
 * - 404 Not Found (or 401 depending on handler)
 * </p>
 */
public class TokenNotFoundException extends AuthGroupException {
	public TokenNotFoundException(String message) {
		super(message);
	}

	public static TokenNotFoundException tokenNotFound() {
		return new TokenNotFoundException("저장된 토큰이 존재하지 않습니다.");
	}
}