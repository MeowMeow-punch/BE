package MeowMeowPunch.pickeat.welstory.dto;

// Welstory 공통 응답 포맷(code/message/data)
public record WelstoryResponse<T>(
	int code,
	String message,
	T data
) {
}
