package MeowMeowPunch.pickeat.domain.diet.dto.request;

import MeowMeowPunch.pickeat.global.common.enums.DietType;

public record RegisterWelstoryDietRequest(
        String groupName,
        String restaurantName,
        String menuName,
        String date,
        DietType mealType
) {}

