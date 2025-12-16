package MeowMeowPunch.pickeat.welstory.exception;

// Welstory API 호출 실패 시 사용하는 기본 예외
public class WelstoryApiException extends RuntimeException {

	public WelstoryApiException(String message) {
		super(message);
	}

	public WelstoryApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
