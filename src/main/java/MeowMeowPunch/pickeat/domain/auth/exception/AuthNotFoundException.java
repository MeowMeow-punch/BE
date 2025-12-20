package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;

/**
 * [Auth][Exception] AuthNotFoundException
 * 요청한 OAuth 계정이나 그룹 정보가 존재하지 않을 때 발생.
 * <p>
 * [HTTP Status]
 * - 404 Not Found
 * - User: "회원이 존재하지 않습니다."
 * - Group: "해당 그룹이 존재하지 않습니다."
 * </p>
 */
public class AuthNotFoundException extends NotFoundGroupException {
	public AuthNotFoundException(String message) {
		super(message);
	}

	public static AuthNotFoundException userNotFound() {
		return new AuthNotFoundException("해당 회원이 존재하지 않습니다.");
	}

	public static AuthNotFoundException groupNotFound() {
		return new AuthNotFoundException("해당 그룹이 존재하지 않습니다.");
	}
}