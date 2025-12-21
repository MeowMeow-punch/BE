package MeowMeowPunch.pickeat.domain.diet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;
import MeowMeowPunch.pickeat.global.common.enums.DietSourceType;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommended_meals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendedDiet extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recommendation_id")
	private Long id;

	@Column(name = "user_id", length = 36, nullable = false)
	private String userId;

	@Column(name = "food_id")
	private Long foodId;

	@Enumerated(EnumType.STRING)
	@Column(name = "diet_status", nullable = false)
	private DietType dietType;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 20)
	private DietSourceType sourceType;

	@Column(name = "date", nullable = false)
	private LocalDate date;

	@Column(name = "time")
	private LocalTime time;

	@Column(name = "title", length = 200, nullable = false)
	private String title;

	@Column(name = "kcal", precision = 8, scale = 2, nullable = false)
	private BigDecimal kcal;

	@Column(name = "carbs", precision = 8, scale = 2, nullable = false)
	private BigDecimal carbs;

	@Column(name = "protein", precision = 8, scale = 2, nullable = false)
	private BigDecimal protein;

	@Column(name = "fat", precision = 8, scale = 2, nullable = false)
	private BigDecimal fat;

	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl;
}
