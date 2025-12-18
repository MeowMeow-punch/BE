package MeowMeowPunch.pickeat.global.llm.core;

// LLM 제공자에 관계없이 공통으로 사용하는 인터페이스
public interface LlmClient {
	LlmResponse generate(LlmRequest request);
}
