package MeowMeowPunch.pickeat.global.common.enums;

import java.util.List;
import java.util.stream.Stream;

// 간식 추천에서 허용하는 카테고리 집합
public enum SnackCategory {
	BAKERY("빵 및 과자"),
	DAIRY("유제품류 및 빙과"),
	BEVERAGE("음료 및 차"),
	FRUIT("과일");

	private final String label;

	SnackCategory(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public static List<String> labels() {
		return Stream.of(values())
			.map(SnackCategory::label)
			.toList();
	}
}
