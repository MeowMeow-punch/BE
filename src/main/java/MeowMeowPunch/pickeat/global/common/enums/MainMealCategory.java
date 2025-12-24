package MeowMeowPunch.pickeat.global.common.enums;

import java.util.List;
import java.util.stream.Stream;

// 메인 식사(아침/점심/저녁) 추천에서 허용하는 카테고리 집합
public enum MainMealCategory {
	RICE("밥류"),
	STEAM("찜류"),
	GRILL("구이류"),
	STIR_FRY("볶음류"),
	BRAISED("조림류"),
	FRY("튀김류"),
	STEW("찌개 및 전골류"),
	SOUP("국 및 탕류"),
	NOODLE("면 및 만두류"),
	MEAT_FISH("수·조·어·육류"),
	BEAN_NUT_SEED("두류, 견과 및 종실류");

	private final String label;

	MainMealCategory(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public static List<String> labels() {
		return Stream.of(values())
			.map(MainMealCategory::label)
			.toList();
	}
}
