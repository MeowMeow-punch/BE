package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.PageInfo;
import MeowMeowPunch.pickeat.global.common.mapper.FoodDtoMapper;

public record FoodSearchResponse(
	int searchNum, List<FoodItem> foods, PageInfo pageInfo
) {
	public static FoodSearchResponse from(List<Food> foods, PageInfo pageInfo) {
		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();

		return new FoodSearchResponse(items.size(), items, pageInfo);
	}
}
