package MeowMeowPunch.pickeat.domain.auth.dto.request;

import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * [Auth][DTO] OAuthLoginRequest
 *
 * 외부 OAuth 로그인 요청을 표현하는 레코드.
 * <p>
 * [Usage]
 * Frontend -> (OAuth Popup) -> Authorization Check
 * Frontend -> [POST /auth/login] -> Backend
 * </p>
 * - 필수 필드: oauthProvider, oauthId, redirectUri
 * - 검증 키워드: redirectUri는 플랫폼 리다이렉트 설정과 일치 필요.
 */
public record OAuthLoginRequest(
		@NotNull(message = "oauthProvider는 필수입니다.") OAuthProvider oauthProvider,
		@NotBlank(message = "oauthId는 필수입니다.") String oauthId,
		@NotBlank(message = "redirectUri는 필수입니다.") String redirectUri) {
}