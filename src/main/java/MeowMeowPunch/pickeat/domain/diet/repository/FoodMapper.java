package MeowMeowPunch.pickeat.domain.diet.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import MeowMeowPunch.pickeat.global.common.dto.response.diet.FoodSummary;

@Mapper
public interface FoodMapper {
	List<FoodSummary> findFoodSummariesForCursor(
		@Param("cursorId") Long cursorId,
		@Param("limit") int limit
	);

	List<FoodSummary> findFoodSummariesByKeyword(
		@Param("keyword") String keyword,
		@Param("cursorId") Long cursorId,
		@Param("limit") int limit
	);

	int findFoodsByKeywordCount(@Param("keyword") String keyword);
}
