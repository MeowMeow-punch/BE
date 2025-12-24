package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.ConflictGroupException;

/**
 * [Diet][Exception] 중복 식단 등록 예외 (409 Conflict)
 */
public class DietDuplicateException extends ConflictGroupException {
    public DietDuplicateException() {
        super("중복저장이 안됩니다");
    }

    public DietDuplicateException(String message) {
        super(message);
    }
}

