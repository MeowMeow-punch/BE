package MeowMeowPunch.pickeat.global.common.enums;

import java.util.List;
import java.util.stream.Stream;

// 메인 식사(아침/점심/저녁) 추천에서 허용하는 카테고리 집합
public enum MainMealCategory {
	RICE("밥"),
	STEAM("찜"),
	GRILL("구이"),
	STIR_FRY("볶음"),
	BRAISED("조림"),
	FRY("튀김"),
	STEW("찌개 및 전골"),
	SOUP("국 및 탕"),
	NOODLE("면 및 만두"),
	MEAT_FISH("어류 및 육류");

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
