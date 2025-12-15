package MeowMeowPunch.pickeat.welstory.api;

public record WelstoryResponse<T>(
	int code,
	String message,
	T data
) {}
