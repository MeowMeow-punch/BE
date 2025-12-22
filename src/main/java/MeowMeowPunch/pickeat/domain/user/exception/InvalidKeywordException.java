package MeowMeowPunch.pickeat.domain.user.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

/**
 * [User][Exception] InvalidKeywordException
 * 검색어 유효성 검사 실패 시 발생.
 * <p>
 * [HTTP Status]
 * - 400 Bad Request
 * </p>
 */
public class InvalidKeywordException extends InvalidGroupException {
    public InvalidKeywordException(String message) {
        super(message);
    }

    public static InvalidKeywordException tooShort() {
        return new InvalidKeywordException("검색어는 최소 2글자 이상이어야 합니다.");
    }

}
