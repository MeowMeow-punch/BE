package MeowMeowPunch.pickeat.domain.user.dto.request;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.ActivityLevel;
import MeowMeowPunch.pickeat.global.common.enums.DrinkingStatus;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.MealFrequency;
import MeowMeowPunch.pickeat.global.common.enums.SmokingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * [User][DTO] FocusUpdateRequest
 * 식단 중점 목표 및 관련 건강 정보 수정 요청
 * - focus 값에 따라 필수 입력 필드가 달라짐 (Service 또는 Validator에서 검증 필요)
 */
public record FocusUpdateRequest(
        @NotNull(message = "식단 중점 목표는 필수입니다.") Focus focus,

        MealFrequency meals,

        ActivityLevel activityLevel,

        @Min(value = 0, message = "목표 몸무게는 0kg 이상이어야 합니다.") @Max(value = 500, message = "목표 몸무게는 500kg 이하여야 합니다.") Integer targetWeight,

        @Size(max = 5, message = "질병 정보는 최대 5개까지만 등록 가능합니다.") List<@Size(max = 50, message = "각 질병 명은 50자를 초과할 수 없습니다.") String> diseases,

        SmokingStatus smokingStatus,

        DrinkingStatus drinkingStatus) {
}
