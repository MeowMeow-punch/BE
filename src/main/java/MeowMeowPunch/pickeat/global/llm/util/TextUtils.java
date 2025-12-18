package MeowMeowPunch.pickeat.global.llm.util;

// 텍스트 전처리/후처리 유틸 (필요 시 확장)
public final class TextUtils {
	private TextUtils() {
	}

	public static String trimToNull(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
