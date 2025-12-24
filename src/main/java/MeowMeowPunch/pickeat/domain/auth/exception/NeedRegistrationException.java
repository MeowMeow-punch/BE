package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;
import lombok.Getter;

@Getter
public class NeedRegistrationException extends NotFoundGroupException {

	private final String registerToken;

	public NeedRegistrationException(String registerToken) {
		super("회원가입이 필요합니다.");
		this.registerToken = registerToken;
	}
}
