package MeowMeowPunch.pickeat.global.llm.util;

/**
 * [Global][LLM] 텍스트 유틸리티
 */
public final class TextUtils {
	private TextUtils() {
	}

	public static String trimToNull(String s) {
		if (s == null)
			return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
