package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.InternalServerErrorGroupException;

/**
 * [Auth][Exception] SocialAuthException
 * 소셜 로그인 서버(카카오, 네이버 등)와의 통신 실패 시 발생.
 * <p>
 * [HTTP Status]
 * - 500 Internal Server Error
 * </p>
 */
public class SocialAuthException extends InternalServerErrorGroupException {

	public SocialAuthException(String message) {
		super(message, null);
	}

	public SocialAuthException(String message, Throwable cause) {
		super(message, cause);
	}

	public static SocialAuthException tokenIssuanceFailed(String provider) {
		return new SocialAuthException(provider + " 액세스 토큰 발급에 실패했습니다.");
	}

	public static SocialAuthException userInfoFailed(String provider) {
		return new SocialAuthException(provider + " 사용자 정보 조회에 실패했습니다.");
	}

	public static SocialAuthException userInfoFailed(String provider, Throwable cause) {
		return new SocialAuthException(provider + " 사용자 정보 조회에 실패했습니다.", cause);
	}

	public static SocialAuthException serverError(String provider, Throwable cause) {
		return new SocialAuthException(provider + " 서버 통신 중 오류가 발생했습니다.", cause);
	}
}
