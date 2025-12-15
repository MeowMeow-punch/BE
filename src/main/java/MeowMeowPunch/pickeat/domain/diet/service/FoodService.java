package MeowMeowPunch.pickeat.domain.diet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodListResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodSearchResponse;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodMapper;
import MeowMeowPunch.pickeat.domain.diet.service.FoodPageAssembler.FoodPage;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodDtoMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodSummary;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {
	private final FoodMapper foodMapper;

	// 음식 리스트 조회
	public FoodListResponse getFoodList(String cursor, Integer size) {
		Long cursorId = FoodPageAssembler.parseCursor(cursor);
		int limit = FoodPageAssembler.resolveLimit(size);

		List<FoodSummary> foods = foodMapper.findFoodSummariesForCursor(cursorId, limit + 1);

		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();

		FoodPage page = FoodPageAssembler.toPage(items, limit);
		return FoodListResponse.of(page.foods(), page.pageInfo());
	}

	// 키워드 기반 음식 리스트 조회
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
