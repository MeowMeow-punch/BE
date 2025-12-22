package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// AI 메시지 응답 DTO
public record AiFeedBack(
	String message,
	String timestamp
) {
	public static AiFeedBack of(String message, String timestamp) {
		return new AiFeedBack(message, timestamp);
	}
}
