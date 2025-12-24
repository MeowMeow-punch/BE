package MeowMeowPunch.pickeat.global.common.dto.response.diet;

// 음식 응답에서 탄단지 영양소 묶음을 표현하는 DTO
public record Nutrients(
	int carbs,
	int protein,
	int fat
) {
	public static Nutrients of(int carbs, int protein, int fat) {
		return new Nutrients(carbs, protein, fat);
	}
}
