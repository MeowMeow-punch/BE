package MeowMeowPunch.pickeat.domain.diet.dto.response;

public record AiFeedBack(
	String message,
	String timestamp
) {
	public static AiFeedBack of(String message, String timestamp) {
		return new AiFeedBack(message, timestamp);
	}
}
