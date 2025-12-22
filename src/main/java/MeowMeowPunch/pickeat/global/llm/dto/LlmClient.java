package MeowMeowPunch.pickeat.global.llm.dto;

// LLM 제공자에 관계없이 공통으로 사용하는 인터페이스

/**
 * [Global][LLM] LLM 클라이언트 인터페이스
 *
 * <pre>
 * Client ──▶ [LlmClient] ──▶ (Provider: OpenAI/Gemini/HighPro)
 * </pre>
 *
 * - LLM 공급자(Provider)에 상관없이 통일된 요청/응답 처리
 */
public interface LlmClient {
	LlmResponse generate(LlmRequest request);
}
