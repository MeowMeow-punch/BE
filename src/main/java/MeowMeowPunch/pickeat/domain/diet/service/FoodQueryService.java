package MeowMeowPunch.pickeat.domain.diet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodListResponse;
import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodSearchResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodMapper;
import MeowMeowPunch.pickeat.domain.diet.service.FoodPageAssembler.FoodPage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodQueryService {
	private final FoodMapper foodMapper;

	public FoodListResponse getFoodList(String cursor, Integer size) {
		Long cursorId = FoodPageAssembler.parseCursor(cursor);
		int limit = FoodPageAssembler.resolveLimit(size);

		// Mybatis 호출
		List<Food> foods = foodMapper.findFoodsForCursor(cursorId, limit + 1);

		FoodPage page = FoodPageAssembler.toPage(foods, limit);
		return FoodListResponse.of(page.foods(), page.pageInfo());
	}

	public FoodSearchResponse search(String keyword, String cursor, Integer size) {
		Long cursorId = FoodPageAssembler.parseCursor(cursor);
		int limit = FoodPageAssembler.resolveLimit(size);

		List<Food> foods = foodMapper.findFoodsByKeyword(keyword, cursorId, limit + 1);
		int totalCount = foodMapper.findFoodsByKeywordCount(keyword);
		FoodPage page = FoodPageAssembler.toPage(foods, limit);

		return FoodSearchResponse.of(page.foods(), page.pageInfo(), totalCount);
	}
}
