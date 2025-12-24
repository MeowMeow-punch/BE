package MeowMeowPunch.pickeat.domain.diet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "my_diet_foods")
@IdClass(DietFoodId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DietFood {

	@Id
	@Column(name = "diet_id", nullable = false)
	private Long dietId;

	@Id
	@Column(name = "food_id", nullable = false)
	private Long foodId;

	@Column(name = "quantity", nullable = false)
	private Short quantity;
}
