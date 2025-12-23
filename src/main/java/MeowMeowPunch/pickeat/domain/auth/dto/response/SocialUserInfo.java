package MeowMeowPunch.pickeat.domain.auth.dto.response;

import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import lombok.Builder;

@Builder
public record SocialUserInfo(
	String id,
	String nickname,
	String email,
	OAuthProvider provider
) {
	public static SocialUserInfo of(String id, String nickname, String email, OAuthProvider provider) {
		return SocialUserInfo.builder()
			.id(id)
			.nickname(nickname)
			.email(email)
			.provider(provider)
			.build();
	}
}
