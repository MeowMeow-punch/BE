package MeowMeowPunch.pickeat.domain.auth.client;

import java.math.BigInteger;
import java.security.SecureRandom;
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
import MeowMeowPunch.pickeat.global.config.NaverProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Auth][Client] NaverAuthClient
 * 
 * 네이버 소셜 로그인 API 연동 구현체.
 * 공식 문서: https://developers.naver.com/docs/login/api/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverAuthClient implements SocialAuthClient {

	private final NaverProperties naverProperties;
	private final RestTemplate restTemplate;

	private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
	private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

	@Override
	public OAuthProvider getProvider() {
		return OAuthProvider.NAVER;
	}

	/**
	 * [Naver] 인가 코드로 액세스 토큰 요청
	 * 
	 * @param code 네이버 인가 코드
	 * @return Access Token
	 * @throws RuntimeException 토큰 발급 실패 시
	 */
	@Override
	public String requestAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		// Naver API does not strictly require charset in header for token, but good practice
		headers.setAcceptCharset(Collections.singletonList(java.nio.charset.StandardCharsets.UTF_8));

		String state = generateState();

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", naverProperties.getClientId());
		body.add("client_secret", naverProperties.getClientSecret());
		body.add("code", code);
		body.add("state", state);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		try {
			// Naver returns JSON with access_token
			ResponseEntity<JsonNode> response = restTemplate.postForEntity(TOKEN_URL, request, JsonNode.class);
			JsonNode responseBody = response.getBody();

			if (responseBody == null || !responseBody.has("access_token")) {
				log.error("[NaverAuth] Token Response Body is null or missing access_token");
				throw new RuntimeException("네이버 액세스 토큰 발급에 실패했습니다.");
			}

			return responseBody.get("access_token").asText();

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("[NaverAuth] Token Request Error: status={}, body={}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw new RuntimeException("네이버 서버 통신 중 오류가 발생했습니다.");
		}
	}

	/**
	 * [Naver] 액세스 토큰으로 사용자 정보 조회
	 * 
	 * @param accessToken 네이버 액세스 토큰
	 * @return SocialUserInfo (ID)
	 */
	@Override
	public SocialUserInfo getUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setAcceptCharset(Collections.singletonList(java.nio.charset.StandardCharsets.UTF_8));

		HttpEntity<Void> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<JsonNode> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request,
					JsonNode.class);
			JsonNode body = response.getBody();

			if (body == null || !body.has("response")) {
				log.error("[NaverAuth] UserInfo Response Body is null or missing 'response' field");
				throw new RuntimeException("네이버 사용자 정보 조회에 실패했습니다.");
			}

			// Naver user info is wrapped in "response" object
			JsonNode responseNode = body.get("response");
			if (!responseNode.has("id")) {
				log.error("[NaverAuth] UserInfo 'response' does not contain 'id'");
				throw new RuntimeException("네이버 사용자 ID를 찾을 수 없습니다.");
			}

			String id = responseNode.get("id").asText();
			return SocialUserInfo.of(id, OAuthProvider.NAVER);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("[NaverAuth] UserInfo Request Error: status={}, body={}", e.getStatusCode(),
					e.getResponseBodyAsString());
			throw new RuntimeException("네이버 사용자 정보 조회 중 오류가 발생했습니다.");
		}
	}

	private String generateState() {
		return new BigInteger(130, new SecureRandom()).toString(32);
	}
}
