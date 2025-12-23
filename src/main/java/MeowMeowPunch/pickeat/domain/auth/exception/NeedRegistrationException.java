package MeowMeowPunch.pickeat.domain.auth.exception;

import MeowMeowPunch.pickeat.domain.auth.dto.response.SocialUserInfo;
import MeowMeowPunch.pickeat.global.error.exception.NotFoundGroupException;
import lombok.Getter;

@Getter
public class NeedRegistrationException extends NotFoundGroupException {

	private final String registerToken;
	private final SocialUserInfo socialUserInfo;

	public NeedRegistrationException(String registerToken, SocialUserInfo socialUserInfo) {
		super("회원가입이 필요합니다.");
		this.registerToken = registerToken;
		this.socialUserInfo = socialUserInfo;
	}
}
