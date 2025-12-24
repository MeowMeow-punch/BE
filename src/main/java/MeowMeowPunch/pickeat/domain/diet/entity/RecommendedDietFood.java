package MeowMeowPunch.pickeat.domain.diet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 추천 식단에 포함된 음식(수량) 매핑 엔티티
@Getter
@Entity
@Table(name = "recommended_diet_foods")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendedDietFood {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recommended_diet_food_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recommendation_id", nullable = false)
	private RecommendedDiet recommendedDiet;

	@Column(name = "food_id")
	private Long foodId;

	@Column(name = "quantity", nullable = false)
	private int quantity;
}
