package MeowMeowPunch.pickeat.domain.auth.dto.response;

import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import lombok.Builder;

@Builder
public record NeedRegistrationResponse(
    String registerToken,
    OAuthProvider provider,
    String email,
    String nickname
) {
    public static NeedRegistrationResponse of(String registerToken, SocialUserInfo userInfo) {
        return NeedRegistrationResponse.builder()
                .registerToken(registerToken)
                .provider(userInfo.provider())
                .email(userInfo.email())
                .nickname(userInfo.nickname())
                .build();
    }
}
