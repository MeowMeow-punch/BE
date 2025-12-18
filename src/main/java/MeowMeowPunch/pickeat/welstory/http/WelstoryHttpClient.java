package MeowMeowPunch.pickeat.welstory.http;

import java.time.Duration;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

// Welstory API 호출용 HttpClient (응답 body를 한 번만 읽고 JSON으로 변환)
public class WelstoryHttpClient {

	private final WebClient webClient;
	private final String deviceId;
	private final Duration timeout;
	private final ObjectMapper objectMapper;

	private volatile String accessToken;

	public WelstoryHttpClient(WebClient webClient, String deviceId, Duration timeout, ObjectMapper objectMapper) {
		this.webClient = webClient;
		this.deviceId = deviceId;
		this.timeout = timeout;
		this.objectMapper = objectMapper;
	}

	// 액세스 토큰 설정
	public void setAccessToken(String token) {
		this.accessToken = token;
	}

	// 현재 액세스 토큰 조회
	public String getAccessToken() {
		return accessToken;
	}

	// GET 요청 수행
	public <T> HttpResult<T> get(String endpoint, Map<String, String> headers,
		ParameterizedTypeReference<T> typeRef) {
		return exchange(HttpMethod.GET, endpoint, headers, null, typeRef);
	}

	// POST 요청 수행
	public <T> HttpResult<T> post(String endpoint, Map<String, String> headers, String body,
		ParameterizedTypeReference<T> typeRef) {
		return exchange(HttpMethod.POST, endpoint, headers, body, typeRef);
	}

	private <T> HttpResult<T> exchange(HttpMethod method,
		String endpoint,
		Map<String, String> headers,
		String body,
		ParameterizedTypeReference<T> typeRef) {

		WebClient.RequestBodySpec base = webClient.method(method)
			.uri(endpoint)
			.headers(h -> applyCommonHeaders(h, headers));

		boolean hasContentTypeHeader = headers != null && headers.containsKey(HttpHeaders.CONTENT_TYPE);
		WebClient.RequestHeadersSpec<?> spec = base;
		if (body != null) {
			if (!hasContentTypeHeader) {
				spec = base.contentType(MediaType.APPLICATION_JSON);
			}
			spec = ((WebClient.RequestBodySpec)spec).bodyValue(body);
		}

		var response = spec.exchangeToMono(mono -> mono.toEntity(String.class)).block(timeout);

		if (response == null) {
			return HttpResult.failure(0, "No response");
		}

		int status = response.getStatusCode().value();
		HttpHeaders resHeaders = response.getHeaders();
		String text = response.getBody();

		T json = null;
		if (text != null && !text.isBlank() && typeRef != null) {
			try {
				json = objectMapper.readValue(
					text,
					objectMapper.getTypeFactory().constructType(typeRef.getType())
				);
			} catch (Exception ignore) {
				// JSON 파싱 실패 시 text 기반 진단을 위해 json은 null로 둔다
			}
		}

		boolean ok = status >= 200 && status < 300;
		return HttpResult.of(ok, status, response.getStatusCode().toString(), resHeaders, json, text);
	}

	// 공통 헤더 구성
	private void applyCommonHeaders(HttpHeaders h, Map<String, String> extra) {
		h.set("User-Agent", "Welplus");
		h.set("X-Device-Id", deviceId);
		if (accessToken != null && !accessToken.isBlank()) {
			h.set("Authorization", accessToken);
		}
		if (extra != null) {
			extra.forEach(h::set);
		}
	}
}
