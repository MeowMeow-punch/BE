package MeowMeowPunch.pickeat.global.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedBackType {
	DAILY("일일 피드백"),
	RECOMMENDATION("추천 식단 피드백");

	private final String description;
}
