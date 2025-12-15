package MeowMeowPunch.pickeat.welstory.exception;

public class WelstoryApiException extends RuntimeException {

	public WelstoryApiException(String message) {
		super(message);
	}

	public WelstoryApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
