package MeowMeowPunch.pickeat.domain.auth.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.auth.client.SocialAuthClient;
import MeowMeowPunch.pickeat.domain.auth.dto.response.SocialUserInfo;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;

@Service
public class CompositeSocialAuthService {

	private final Map<OAuthProvider, SocialAuthClient> clientMap;

	public CompositeSocialAuthService(List<SocialAuthClient> clients) {
		this.clientMap = clients.stream()
			.collect(Collectors.toMap(SocialAuthClient::getProvider, Function.identity()));
	}

	public SocialUserInfo getUserInfo(OAuthProvider provider, String authorizationCode) {
		SocialAuthClient client = clientMap.get(provider);
		if (client == null) {
			throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
		}

		String accessToken = client.requestAccessToken(authorizationCode);
		return client.getUserInfo(accessToken);
	}
}
