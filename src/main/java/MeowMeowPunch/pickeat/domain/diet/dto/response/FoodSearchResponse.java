package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.dto.response.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.PageInfo;

public record FoodSearchResponse(
	int searchNum, List<FoodItem> foods, PageInfo pageInfo
) {
	public static FoodSearchResponse of(List<FoodItem> foods, PageInfo pageInfo, int totalCount) {
		return new FoodSearchResponse(totalCount, foods, pageInfo);
	}
}
