package MeowMeowPunch.pickeat.domain.auth.dto.response;

import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import lombok.Builder;

@Builder
public record SocialUserInfo(
		String id,
		OAuthProvider provider) {
	public static SocialUserInfo of(String id, OAuthProvider provider) {
		return SocialUserInfo.builder()
				.id(id)
				.provider(provider)
				.build();
	}
}
