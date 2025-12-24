package MeowMeowPunch.pickeat.welstory.http;

import org.springframework.http.HttpHeaders;

// WebClient 호출 결과를 JSON과 원문 텍스트를 함께 담는 래퍼
public record HttpResult<T>(
	boolean ok,
	int status,
	String statusText,
	HttpHeaders headers,
	T json,
	String text
) {
	public static <T> HttpResult<T> of(
		boolean ok,
		int status,
		String statusText,
		HttpHeaders headers,
		T json,
		String text
	) {
		return new HttpResult<>(ok, status, statusText, headers, json, text);
	}

	public static <T> HttpResult<T> failure(int status, String statusText) {
		return new HttpResult<>(false, status, statusText, new HttpHeaders(), null, null);
	}
}
