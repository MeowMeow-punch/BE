package MeowMeowPunch.pickeat.domain.diet.dto.response;

public record DietRegisterResponse(Long myDietId) {
	public static DietRegisterResponse from(Long myDietId) {
		return new DietRegisterResponse(myDietId);
	}
}
