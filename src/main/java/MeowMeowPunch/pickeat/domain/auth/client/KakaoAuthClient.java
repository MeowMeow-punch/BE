package MeowMeowPunch.pickeat.domain.auth.client;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import MeowMeowPunch.pickeat.domain.auth.dto.response.SocialUserInfo;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import MeowMeowPunch.pickeat.global.config.KakaoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Auth][Client] KakaoAuthClient
 * 
 * 카카오 소셜 로그인 API 연동 구현체.
 * 공식 문서: https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAuthClient implements SocialAuthClient {

	private final KakaoProperties kakaoProperties;
	private final RestTemplate restTemplate;

	private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
	private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.KAKAO;
	}

	/**
	 * [Kakao] 인가 코드로 액세스 토큰 요청
	 * 
	 * @param code 카카오 인가 코드
	 * @return Access Token
	 * @throws RuntimeException 토큰 발급 실패 시
	 */
	@Override
	public String requestAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", kakaoProperties.getClientId());
		body.add("redirect_uri", kakaoProperties.getRedirectUri());
		body.add("code", code);
		if (kakaoProperties.getClientSecret() != null) {
			body.add("client_secret", kakaoProperties.getClientSecret());
		}

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<JsonNode> response = restTemplate.postForEntity(TOKEN_URL, request, JsonNode.class);

			if (response.getBody() == null || !response.getBody().has("access_token")) {
				log.error("[KakaoAuth] Token Response Body is null or missing access_token");
				throw new RuntimeException("카카오 액세스 토큰 발급에 실패했습니다.");
			}

			return response.getBody().get("access_token").asText();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("[KakaoAuth] Token Request Error: status={}, body={}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw new RuntimeException("카카오 서버 통신 중 오류가 발생했습니다.");
		}
	}

	/**
	 * [Kakao] 액세스 토큰으로 사용자 정보 조회
	 * 
	 * @param accessToken 카카오 액세스 토큰
	 * @return SocialUserInfo (ID, Nickname, Email)
	 */
	@Override
	public SocialUserInfo getUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setBearerAuth(accessToken);
		headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

		HttpEntity<Void> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<JsonNode> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request,
					JsonNode.class);
			JsonNode body = response.getBody();

			if (body == null) {
				log.error("[KakaoAuth] UserInfo Response Body is null");
				throw new RuntimeException("카카오 사용자 정보 조회에 실패했습니다.");
			}

			String id = String.valueOf(body.get("id").asLong());
			return SocialUserInfo.of(id, OAuthProvider.KAKAO);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("[KakaoAuth] UserInfo Request Error: status={}, body={}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw new RuntimeException("카카오 사용자 정보 조회 중 오류가 발생했습니다.");
		}
	}
}
