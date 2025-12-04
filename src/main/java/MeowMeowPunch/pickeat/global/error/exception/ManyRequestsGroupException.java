package MeowMeowPunch.pickeat.global.error.exception;

public abstract class ManyRequestsGroupException extends RuntimeException{
	public ManyRequestsGroupException(String message) {
		super(message);
	}
}
