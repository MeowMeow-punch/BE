package MeowMeowPunch.pickeat.domain.diet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodListResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodSearchResponse;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodMapper;
import MeowMeowPunch.pickeat.domain.diet.service.FoodPageAssembler.FoodPage;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.FoodSummary;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodQueryService {
	private final FoodMapper foodMapper;

	public FoodListResponse getFoodList(String cursor, Integer size) {
		Long cursorId = FoodPageAssembler.parseCursor(cursor);
		int limit = FoodPageAssembler.resolveLimit(size);

		// Mybatis 호출
		List<FoodSummary> foods = foodMapper.findFoodSummariesForCursor(cursorId, limit + 1);

		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();

		FoodPage page = FoodPageAssembler.toPage(items, limit);
		return FoodListResponse.of(page.foods(), page.pageInfo());
	}

	public FoodSearchResponse search(String keyword, String cursor, Integer size) {
		Long cursorId = FoodPageAssembler.parseCursor(cursor);
		int limit = FoodPageAssembler.resolveLimit(size);

		List<FoodSummary> foods = foodMapper.findFoodSummariesByKeyword(keyword, cursorId, limit + 1);
		int totalCount = foodMapper.findFoodsByKeywordCount(keyword);
		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();

		FoodPage page = FoodPageAssembler.toPage(items, limit);

		return FoodSearchResponse.of(page.foods(), page.pageInfo(), totalCount);
	}
}
