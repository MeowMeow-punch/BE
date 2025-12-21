package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.ConflictGroupException;

/**
 * [Auth][Exception] DuplicateNicknameException
 * 이미 등록된 닉네임으로 회원가입을 시도할 때 발생.
 * <p>
 * [HTTP Status]
 * - 409 Conflict
 * </p>
 */
public class DuplicateNicknameException extends ConflictGroupException {
	public DuplicateNicknameException(String message) {
		super(message);
	}

	public static DuplicateNicknameException duplicateNickname() {
		return new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
	}
}
