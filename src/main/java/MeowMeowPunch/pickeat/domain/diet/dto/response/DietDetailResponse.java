package MeowMeowPunch.pickeat.domain.diet.dto.response;

// 식단 상세 조회 응답 DTO
public record DietDetailResponse(
	DietInfo dietInfo
) {
	public static DietDetailResponse of(DietInfo dietInfo) {
		return new DietDetailResponse(dietInfo);
	}
}
