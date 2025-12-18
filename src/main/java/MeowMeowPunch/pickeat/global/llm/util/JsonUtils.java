package MeowMeowPunch.pickeat.global.llm.util;

import com.fasterxml.jackson.databind.ObjectMapper;

// JSON 직렬화/역직렬화 보조 유틸 (필요 시 확장)
public final class JsonUtils {
	private static final ObjectMapper mapper = new ObjectMapper();

	private JsonUtils() {
	}

	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException("JSON 직렬화 실패", e);
		}
	}
}
