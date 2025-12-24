package MeowMeowPunch.pickeat.global.common.enums;

/**
 * [Auth][Enum] MealFrequency
 * 회원의 하루 식사 횟수를 표현
 */
public enum MealFrequency {
	ONE(1),
	TWO(2),
	THREE(3),
	ETC(3);

	private final int count;

	MealFrequency(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}
}