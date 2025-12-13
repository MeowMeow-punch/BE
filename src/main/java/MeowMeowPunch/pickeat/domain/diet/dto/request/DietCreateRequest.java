package MeowMeowPunch.pickeat.domain.diet.dto.request;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.DietType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DietCreateRequest(
	@NotBlank(message = "date는 필수입니다.")
	String date,

	@NotNull(message = "mealType은 필수입니다.")
	DietType mealType,

	@NotBlank(message = "time은 필수입니다.")
	String time,

	@Size(min = 1, message = "최소 1개의 음식 정보가 필요합니다.")
	List<@Valid FoodQuantity> foods
) {
	public record FoodQuantity(
		@NotNull(message = "foodId는 필수입니다.")
		Long foodId,

		@Positive(message = "quantity는 1 이상이어야 합니다.")
		Integer quantity
	) {
	}
}
