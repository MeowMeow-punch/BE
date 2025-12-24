package MeowMeowPunch.pickeat.global.common.dto.response.diet;

import java.util.Map;

import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;

public final class RecommendedDietInfoContext {
    private RecommendedDietInfoContext() {}

    private static final ThreadLocal<Map<Long, DietSourceType>> SOURCE_TYPE_MAP = new ThreadLocal<>();

    public static void set(Map<Long, DietSourceType> map) {
        SOURCE_TYPE_MAP.set(map);
    }

    public static DietSourceType resolve(Long id) {
        Map<Long, DietSourceType> map = SOURCE_TYPE_MAP.get();
        if (map == null) {
            return DietSourceType.FOOD_DB;
        }
        return map.getOrDefault(id, DietSourceType.FOOD_DB);
    }

    public static void clear() {
        SOURCE_TYPE_MAP.remove();
    }
}
