package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import MeowMeowPunch.pickeat.domain.diet.entity.Food;

@Mapper
public interface FoodMapper {
	List<Food> findFoodsForCursor(
		@Param("cursorId") Long cursorId,
		@Param("limit") int limit
	);

	List<Food> findFoodsByKeyword(
		@Param("keyword") String keyword,
		@Param("cursorId") Long cursorId,
		@Param("limit") int limit
	);
}
