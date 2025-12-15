package MeowMeowPunch.pickeat.welstory.exception;

public class WelstoryBadResponseException extends RuntimeException {

	public WelstoryBadResponseException(String message) {
		super(message);
	}

	public WelstoryBadResponseException(String message, Throwable cause) {
		super(message, cause);
	}
}
