package MeowMeowPunch.pickeat.domain.diet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import MeowMeowPunch.pickeat.domain.diet.service.DietPageAssembler;
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
@Table(name = "my_diets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Diet extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "diet_id")
	private Long id;

	@Column(name = "user_id", length = 36, nullable = false)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private DietType status;

	@Column(name = "title", length = 200, nullable = false)
	private String title;

	@Column(name = "date", nullable = false)
	private LocalDate date;

	@Column(name = "time")
	private LocalTime time;

	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false)
	@Builder.Default
	private DietSourceType sourceType = DietSourceType.USERINPUT;

	@Column(name = "kcal", precision = 8, scale = 2, nullable = false)
	private BigDecimal kcal;

	@Column(name = "carbs", precision = 8, scale = 2, nullable = false)
	private BigDecimal carbs;

	@Column(name = "protein", precision = 8, scale = 2, nullable = false)
	private BigDecimal protein;

	@Column(name = "fat", precision = 8, scale = 2, nullable = false)
	private BigDecimal fat;

	@Column(name = "sugar", precision = 8, scale = 2, nullable = false)
	private BigDecimal sugar;

	@Column(name = "vit_a", precision = 8, scale = 2)
	private BigDecimal vitA;

	@Column(name = "vit_c", precision = 8, scale = 2)
	private BigDecimal vitC;

	@Column(name = "vit_d", precision = 8, scale = 2)
	private BigDecimal vitD;

	@Column(name = "calcium", precision = 8, scale = 2)
	private BigDecimal calcium;

	@Column(name = "iron", precision = 8, scale = 2)
	private BigDecimal iron;

	@Column(name = "dietary_fiber", precision = 8, scale = 2)
	private BigDecimal dietaryFiber;

	@Column(name = "sodium", precision = 8, scale = 2)
	private BigDecimal sodium;

	// 사용자 입력 식단 생성용 팩토리
	public static Diet createUserInput(
		String userId,
		DietType status,
		LocalDate date,
		LocalTime time,
		DietPageAssembler.DietAggregation aggregation
	) {
		return Diet.builder()
			.userId(userId)
			.status(status)
			.title(aggregation.title())
			.date(date)
			.time(time)
			.thumbnailUrl(aggregation.thumbnailUrl())
			.sourceType(DietSourceType.USERINPUT)
			.kcal(aggregation.kcal())
			.carbs(aggregation.carbs())
			.protein(aggregation.protein())
			.fat(aggregation.fat())
			.sugar(aggregation.sugar())
			.vitA(aggregation.vitA())
			.vitC(aggregation.vitC())
			.vitD(aggregation.vitD())
			.calcium(aggregation.calcium())
			.iron(aggregation.iron())
			.dietaryFiber(aggregation.dietaryFiber())
			.sodium(aggregation.sodium())
			.build();
	}
}
