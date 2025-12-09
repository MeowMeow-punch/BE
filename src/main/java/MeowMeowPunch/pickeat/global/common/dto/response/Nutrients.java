package MeowMeowPunch.pickeat.global.common.dto.response;

public record Nutrients(
	int carbs,
	int protein,
	int fat
) {
	public static Nutrients of(int carbs, int protein, int fat) {
		return new Nutrients(carbs, protein, fat);
	}
}
