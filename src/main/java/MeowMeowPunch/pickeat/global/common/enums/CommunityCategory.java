package MeowMeowPunch.pickeat.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityCategory {
	DIET("다이어트"),
	EXERCISE("운동"),
	NUTRIENT("영양"),
	DISEASE("질병관리");

	private final String description;
}