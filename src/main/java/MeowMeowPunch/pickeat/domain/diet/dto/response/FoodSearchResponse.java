package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.PageInfo;

public record FoodSearchResponse(
	int searchNum, List<FoodItem> foods, PageInfo pageInfo
) {
	public static FoodSearchResponse of(List<Food> foods, PageInfo pageInfo, int totalCount) {
		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();

		return new FoodSearchResponse(totalCount, items, pageInfo);
	}
}
