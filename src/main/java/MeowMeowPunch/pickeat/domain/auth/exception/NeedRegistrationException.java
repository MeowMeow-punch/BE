package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;
import lombok.Getter;

@Getter
public class NeedRegistrationException extends NotFoundGroupException {

	private final String oauthId;
	private final OAuthProvider oauthProvider;

	public NeedRegistrationException(String oauthId, OAuthProvider oauthProvider) {
		super("회원가입이 필요합니다.");
		this.oauthId = oauthId;
		this.oauthProvider = oauthProvider;
	}
}
