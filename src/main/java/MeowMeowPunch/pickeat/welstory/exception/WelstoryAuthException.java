package MeowMeowPunch.pickeat.welstory.exception;

// Welstory 인증 실패 시 사용하는 예외
public class WelstoryAuthException extends RuntimeException {

	public WelstoryAuthException(String message) {
		super(message);
	}

	public WelstoryAuthException(String message, Throwable cause) {
		super(message, cause);
	}
}
