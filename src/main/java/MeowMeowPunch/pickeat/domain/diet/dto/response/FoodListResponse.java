package MeowMeowPunch.pickeat.domain.diet.dto.response;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.PageInfo;

// 음식 목록 리스트 응답 DTO
public record FoodListResponse(
	List<FoodItem> foods,
	PageInfo pageInfo
) {
	public static FoodListResponse of(List<FoodItem> foods, PageInfo pageInfo) {
		return new FoodListResponse(foods, pageInfo);
	}
}
