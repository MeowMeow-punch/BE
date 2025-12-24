package MeowMeowPunch.pickeat.domain.diet.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public FoodListResponse getFoodList(String cursor, Integer size, String category) {
        Long cursorId = FoodPageAssembler.parseCursor(cursor);
        int limit = FoodPageAssembler.resolveLimit(size);

        List<String> categoryPatterns = toCategoryPatterns(category);
        List<FoodSummary> foods = foodMapper.findFoodSummariesForCursor(cursorId, limit + 1, categoryPatterns);

		List<FoodItem> items = foods.stream()
			.map(FoodDtoMapper::toFoodItem)
			.toList();

		FoodPage page = FoodPageAssembler.toPage(items, limit);
		return FoodListResponse.of(page.foods(), page.pageInfo());
	}

    // 키워드 기반 음식 리스트 조회
    public FoodSearchResponse search(String keyword, String cursor, Integer size, String category) {
        Long cursorId = FoodPageAssembler.parseCursor(cursor);
        int limit = FoodPageAssembler.resolveLimit(size);

        List<String> categoryPatterns = toCategoryPatterns(category);
        List<FoodSummary> foods = foodMapper.findFoodSummariesByKeyword(keyword, cursorId, limit + 1, categoryPatterns);

        int totalCount = foodMapper.findFoodsByKeywordCount(keyword, categoryPatterns);
        List<FoodItem> items = foods.stream()
                .map(FoodDtoMapper::toFoodItem)
                .toList();

		FoodPage page = FoodPageAssembler.toPage(items, limit);

        return FoodSearchResponse.of(page.foods(), page.pageInfo(), totalCount);
    }

    // 프론트 한글 카테고리 입력을 SQL ILIKE 패턴으로 변환
    private List<String> toCategoryPatterns(String category) {
        if (category == null) {
            return null;
        }
        String raw = category.trim();
        if (raw.isEmpty()) {
            return null;
        }
        // 구분자('/', ',', 공백, '및')를 기준으로 토큰화하고 각 토큰을 포함 검색으로 변환
        String normalized = raw.replaceAll("\\s+", " ");
        String[] tokens = normalized
                .replace("및", "/")
                .replace(",", "/")
                .replace(" ", "")
                .split("/");

        List<String> patterns = new ArrayList<>();
        for (String t : tokens) {
            if (t == null) continue;
            String token = t.trim();
            if (token.isEmpty()) continue;
            patterns.add("%" + token + "%");          // 예: 밥 -> %밥%
            // '류' 가 붙은 저장 형태도 대비 (예: 밥류)
            if (!token.endsWith("류")) {
                patterns.add("%" + token + "류%");
            }
        }
        return patterns.isEmpty() ? null : patterns;
    }
}
