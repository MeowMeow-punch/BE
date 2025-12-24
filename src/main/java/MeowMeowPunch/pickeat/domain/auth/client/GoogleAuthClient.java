package MeowMeowPunch.pickeat.domain.auth.client;

import java.net.URLDecoder;
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
import MeowMeowPunch.pickeat.domain.auth.exception.SocialAuthException;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import MeowMeowPunch.pickeat.global.config.GoogleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Auth][Client] GoogleAuthClient
 * 
 * 구글 소셜 로그인 API 연동 구현체.
 * 공식 문서: https://developers.google.com/identity/protocols/oauth2/web-server
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthClient implements SocialAuthClient {

	private final GoogleProperties googleProperties;
	private final RestTemplate restTemplate;

	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.GOOGLE;
	}

	/**
	 * [Google] 인가 코드로 액세스 토큰 요청
	 * 
	 * @param code 구글 인가 코드
	 * @return Access Token
	 * @throws SocialAuthException 토큰 발급 실패 시
	 */
	@Override
	public String requestAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// 구글은 redirect_uri가 인코딩된 상태로 오면 이중 인코딩 문제가 발생할 수 있으므로 디코딩 처리
		String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", googleProperties.getClientId());
		body.add("client_secret", googleProperties.getClientSecret());
		body.add("redirect_uri", googleProperties.getRedirectUri());
		body.add("code", decodedCode);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<JsonNode> response = restTemplate.postForEntity(TOKEN_URL, request, JsonNode.class);

			if (response.getBody() == null || !response.getBody().has("access_token")) {
				log.error("[GoogleAuth] 토큰 응답 본문이 비어있거나 access_token이 없습니다.");
				throw SocialAuthException.tokenIssuanceFailed("구글");
			}

			return response.getBody().get("access_token").asText();

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("[GoogleAuth] 토큰 발급 요청 실패: status={}, body={}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw SocialAuthException.serverError("구글", e);
		}
	}

	/**
	 * [Google] 액세스 토큰으로 사용자 정보 조회
	 * 
	 * @param accessToken 구글 액세스 토큰
	 * @return SocialUserInfo (ID)
	 */
	@Override
	public SocialUserInfo getUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		HttpEntity<Void> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<JsonNode> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request,
					JsonNode.class);
			JsonNode body = response.getBody();

			if (body == null) {
				log.error("[GoogleAuth] 사용자 정보 응답 본문이 비어있습니다.");
				throw SocialAuthException.userInfoFailed("구글");
			}

			if (!body.has("id")) {
				log.error("[GoogleAuth] 사용자 정보 응답에 'id' 필드가 없습니다.");
				throw SocialAuthException.userInfoFailed("구글");
			}

			String id = body.get("id").asText();
			return SocialUserInfo.of(id, OAuthProvider.GOOGLE);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("[GoogleAuth] 사용자 정보 조회 요청 실패: status={}, body={}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw SocialAuthException.userInfoFailed("구글", e);
		}
	}
}
