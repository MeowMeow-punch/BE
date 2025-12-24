package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.ConflictGroupException;

/**
 * [Auth][Exception] DuplicateUserException
 * 이미 가입된 사용자가 다시 가입을 시도할 때 발생.
 * <p>
 * [HTTP Status]
 * - 409 Conflict
 * </p>
 */
public class DuplicateUserException extends ConflictGroupException {
    public DuplicateUserException(String message) {
        super(message);
    }

    public static DuplicateUserException duplicateUser() {
        return new DuplicateUserException("이미 가입된 회원입니다. 로그인을 진행해주세요.");
    }
}
