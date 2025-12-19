package MeowMeowPunch.pickeat.domain.diet.exception;

public class DietNotEditableException extends RuntimeException {
	public DietNotEditableException(Long dietId) {
		super("식단을 수정/삭제할 수 없습니다. dietId=" + dietId);
	}
}
