package MeowMeowPunch.pickeat.domain.diet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.diet.dto.response.FoodListResponse;
import MeowMeowPunch.pickeat.domain.diet.entity.Food;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietCursorException;
import MeowMeowPunch.pickeat.domain.diet.exception.InvalidDietPageSizeException;
import MeowMeowPunch.pickeat.domain.diet.repository.FoodMapper;
import MeowMeowPunch.pickeat.global.common.dto.response.PageInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodQueryService {
	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 50;

	private final FoodMapper foodMapper;

	public FoodListResponse getFoodList(String cursor, Integer size) {
		Long cursorId = parseCursor(cursor);
		int limit = resolveLimit(size);

		// Mybatis 호출
		List<Food> foods = foodMapper.findFoodsForCursor(cursorId, limit + 1);

		boolean hasNext = foods.size() > limit;

		if (hasNext) {
			foods = foods.subList(0, limit);
		}

		String nextCursor = hasNext && !foods.isEmpty() ? String.valueOf(foods.get(foods.size() - 1).getId()) : null;

		PageInfo pageInfo = new PageInfo(nextCursor, hasNext);
		return FoodListResponse.from(foods, pageInfo);
	}

	// == 내부 헬퍼 메서드 ==

	private Long parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(cursor);
		} catch (NumberFormatException e) {
			// 잘못된 cursor 값 => 400 InvalidDietCursorException
			throw new InvalidDietCursorException(cursor);
		}
	}

	private int resolveLimit(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_LIMIT;
		}
		if (size > MAX_LIMIT) {
			// 과도하게 큰 size 요청 => 400 InvalidDietPageSizeException
			throw new InvalidDietPageSizeException(size, MAX_LIMIT);
		}
		return size;
	}
}
