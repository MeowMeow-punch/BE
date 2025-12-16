package MeowMeowPunch.pickeat.welstory;

import java.util.Map;
import java.util.function.Supplier;

import MeowMeowPunch.pickeat.welstory.dto.WelstoryResponse;
import MeowMeowPunch.pickeat.welstory.exception.WelstoryApiException;
import MeowMeowPunch.pickeat.welstory.exception.WelstoryAuthException;
import MeowMeowPunch.pickeat.welstory.exception.WelstoryBadResponseException;
import MeowMeowPunch.pickeat.welstory.http.HttpResult;
import MeowMeowPunch.pickeat.welstory.http.WelstoryHttpClient;

// Welstory 외부 API 호출을 총괄하고 인증/재시도를 관리하는 클라이언트
public class WelstoryClient {

	private final WelstoryHttpClient http;
	private final String username;
	private final String password;

	public WelstoryClient(WelstoryHttpClient http, String username, String password) {
		this.http = http;
		this.username = username;
		this.password = password;
	}

	// 토큰이 없으면 로그인까지 수행해 인증 보장
	public void ensureToken() {
		if (http.getAccessToken() == null || http.getAccessToken().isBlank()) {
			if (username != null && password != null) {
				loginInternal();
			} else {
				throw new WelstoryApiException("Welstory access token is missing. login/refresh가 선행돼야 합니다.");
			}
		}
	}

	// Welstory 인증 토큰 발급
	private void loginInternal() {
		String body = "username=" + urlEncode(username)
			+ "&password=" + urlEncode(password)
			+ "&remember-me=false";

		var res = http.post(Endpoints.LOGIN, Map.of(
			"Content-Type", "application/x-www-form-urlencoded",
			"X-Autologin", "N"
		), body, null);

		if (!res.ok()) {
			throw new WelstoryAuthException("Welstory login failed: " + res.status() + " " + safe(res.text()));
		}

		String token = res.headers().getFirst("Authorization");
		if (token == null || token.isBlank()) {
			throw new WelstoryBadResponseException("Welstory login: Authorization header missing");
		}
		http.setAccessToken(token);
	}

	// WelstoryRestaurant 생성 헬퍼
	public WelstoryRestaurant restaurant(String restaurantId, String name, String desc) {
		return new WelstoryRestaurant(this, restaurantId, name, desc);
	}

	WelstoryHttpClient http() {
		return http;
	}

	private static String urlEncode(String s) {
		return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
	}

	private static String safe(String t) {
		if (t == null) {
			return null;
		}
		return t.length() > 400 ? t.substring(0, 400) + "..." : t;
	}

	// WelstoryResponse 검증
	public <T> WelstoryResponse<T> unwrap(HttpResult<WelstoryResponse<T>> res, String action) {
		if (!res.ok() || res.json() == null) {
			throw new WelstoryApiException(action + " HTTP 실패: " + res.status() + " body=" + safe(res.text()));
		}
		WelstoryResponse<T> body = res.json();
		if (body.code() != 0) {
			throw new WelstoryApiException(action + " 실패 code=" + body.code() + ", message=" + body.message());
		}
		if (body.data() == null) {
			throw new WelstoryApiException(action + " 성공이지만 data가 null 입니다.");
		}
		return body;
	}

	boolean isAuthFailure(int status) {
		return status == 401 || status == 403;
	}

	// 401/403 자동 재시도
	<T> HttpResult<WelstoryResponse<T>> callWithRetry(
		Supplier<HttpResult<WelstoryResponse<T>>> caller) {
		for (int attempt = 0; attempt < 2; attempt++) {
			HttpResult<WelstoryResponse<T>> res = caller.get();
			boolean emptyBody = res.text() == null || res.text().isBlank();
			if (!isAuthFailure(res.status()) && !emptyBody) {
				return res;
			}
			// 첫 호출이 401/403이면 재로그인 후 한 번 더 시도
			http.setAccessToken(null); // 만료 토큰 제거 후 재발급
			loginInternal();
		}
		// 재시도 후에도 실패 시 마지막 결과 반환
		return caller.get();
	}
}
