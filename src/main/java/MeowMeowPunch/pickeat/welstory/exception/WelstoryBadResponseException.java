package MeowMeowPunch.pickeat.welstory.exception;

// Welstory 응답 포맷이 예상과 다를 때 사용하는 예외
public class WelstoryBadResponseException extends RuntimeException {

	public WelstoryBadResponseException(String message) {
		super(message);
	}

	public WelstoryBadResponseException(String message, Throwable cause) {
		super(message, cause);
	}
}
