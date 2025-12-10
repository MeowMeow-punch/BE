package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.PageInfo;

// 음식 목록 리스트 응답 DTO
public record FoodListResponse(
	List<FoodItem> foods,
	PageInfo pageInfo
) {
	public static FoodListResponse of(List<Food> foods, PageInfo pageInfo) {
		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();
		return new FoodListResponse(items, pageInfo);
	}
}
