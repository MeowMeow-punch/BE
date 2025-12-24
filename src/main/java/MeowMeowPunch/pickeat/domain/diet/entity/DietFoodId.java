package MeowMeowPunch.pickeat.domain.diet.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;

public class DietFoodId implements Serializable {
	@Column(name = "diet_id")
	private Long dietId;

	@Column(name = "food_id")
	private Long foodId;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DietFoodId that = (DietFoodId)o;
		return Objects.equals(dietId, that.dietId) && Objects.equals(foodId, that.foodId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dietId, foodId);
	}
}
