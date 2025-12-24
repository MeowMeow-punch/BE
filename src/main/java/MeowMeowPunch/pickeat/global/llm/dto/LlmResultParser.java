package MeowMeowPunch.pickeat.global.llm.dto;

import MeowMeowPunch.pickeat.global.llm.exception.LlmParsingException;

/**
 * [Global][LLM] 응답 파싱 유틸리티
 *
 * - Markdown 코드 블록 제거
 * - JSON 객체 범위 추출
 */
public final class LlmResultParser {
	private LlmResultParser() {
	}

	/**
	 * LLM 응답 텍스트에서 JSON 부분만 추출
	 *
	 * @param text 원본 응답 텍스트 (Markdown 포함 가능)
	 * @return 순수 JSON 문자열
	 */
	public static String extractJsonOrThrow(String text) {
		if (text == null || text.isBlank()) {
			throw new LlmParsingException("LLM response text is empty");
		}

		String cleaned = text.trim();

		// 1. Markdown Code Block 제거 (```json ... ```)
		if (cleaned.startsWith("```")) {
			int firstBreak = cleaned.indexOf('\n');
			if (firstBreak > 0) {
				cleaned = cleaned.substring(firstBreak + 1);
			}
			if (cleaned.endsWith("```")) {
				cleaned = cleaned.substring(0, cleaned.length() - 3);
			}
		}

		// 2. 가장 바깥쪽 중괄호 탐색 ({ ... })
		int start = cleaned.indexOf('{');
		int end = cleaned.lastIndexOf('}');

		if (start == -1 || end == -1 || start > end) {
			// JSON 형식이 아니라고 판단 -> 원문이 단순 텍스트일 수도 있으나,
			// 이 메서드는 "JSON 추출"이 목적이므로 에러 혹은 원문 반환 정책 결정 필요.
			// 여기서는 최대한 원문을 trim해서 반환하되, 파싱 에러는 상위(ObjectMapper)에서 잡게 함.
			return cleaned.trim();
		}

		return cleaned.substring(start, end + 1).trim();
	}
}
