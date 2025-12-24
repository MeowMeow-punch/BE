package MeowMeowPunch.pickeat.domain.auth.client;

import MeowMeowPunch.pickeat.domain.auth.dto.response.SocialUserInfo;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;

public interface SocialAuthClient {
	OAuthProvider getProvider();
	String requestAccessToken(String code);
	SocialUserInfo getUserInfo(String accessToken);
}
