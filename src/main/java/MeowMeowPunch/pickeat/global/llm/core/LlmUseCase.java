package MeowMeowPunch.pickeat.global.llm.core;

// LLM 활용 목적을 구분하는 enum
public enum LlmUseCase {
	HOME_RECOMMEND,  // 후보 6개 중 1~2개 + 한줄 이유
	DAILY_FEEDBACK   // 오늘 섭취 평가 카드
}
