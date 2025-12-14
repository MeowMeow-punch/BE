package MeowMeowPunch.pickeat.domain.diet.service;

import java.util.List;

import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietCursorException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietPageSizeException;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodItem;
import MeowMeowPunch.pickeat.global.common.dto.response.diet.PageInfo;

// 음식 목록 커서 결과를 가공하는 헬퍼
public final class FoodPageAssembler {
	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 50;

	private FoodPageAssembler() {
	}

	public static FoodPage toPage(List<FoodItem> foods, int limit) {
		boolean hasNext = foods.size() > limit;
		List<FoodItem> pageFoods = hasNext ? foods.subList(0, limit) : foods;
		String nextCursor = hasNext && !pageFoods.isEmpty()
			? String.valueOf(pageFoods.get(pageFoods.size() - 1).foodId())
			: null;

		PageInfo pageInfo = hasNext ? PageInfo.of(nextCursor, true) : PageInfo.empty();
		return new FoodPage(pageFoods, pageInfo);
	}

	public static Long parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(cursor);
		} catch (NumberFormatException e) {
			throw new InvalidDietCursorException(cursor);
		}
	}

	public static int resolveLimit(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_LIMIT;
		}
		if (size > MAX_LIMIT) {
			throw new InvalidDietPageSizeException(size, MAX_LIMIT);
		}
		return size;
	}

	public record FoodPage(List<FoodItem> foods, PageInfo pageInfo) {
	}
}
