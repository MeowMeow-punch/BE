package MeowMeowPunch.pickeat.domain.auth.dto.response;

import lombok.Builder;

@Builder
public record NeedRegistrationResponse(
        String registerToken) {
    public static NeedRegistrationResponse of(String registerToken) {
        return NeedRegistrationResponse.builder()
                .registerToken(registerToken)
                .build();
    }
}
