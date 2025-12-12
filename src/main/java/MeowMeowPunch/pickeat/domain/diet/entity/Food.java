package MeowMeowPunch.pickeat.domain.diet.entity;

import java.math.BigDecimal;

import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;
import MeowMeowPunch.pickeat.global.common.enums.FoodBaseUnit;
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
@Table(name = "foods")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Food extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "food_code", length = 50, nullable = false, unique = true)
	private String foodCode;

	@Column(name = "name", length = 200, nullable = false)
	private String name;

	@Column(name = "category", length = 50, nullable = false)
	private String category;

	@Column(name = "base_amount", nullable = false)
	private Integer baseAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "base_unit", length = 10, nullable = false)
	private FoodBaseUnit baseUnit;

	@Column(name = "serving_size", precision = 6, scale = 2)
	private BigDecimal servingSize;

	@Column(name = "serving_desc", length = 100)
	private String servingDesc;

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

	@Column(name = "dietary_fiber", precision = 8, scale = 2, nullable = false)
	private BigDecimal dietaryFiber;

	@Column(name = "vit_a", precision = 8, scale = 2, nullable = false)
	private BigDecimal vitA;

	@Column(name = "vit_c", precision = 8, scale = 2, nullable = false)
	private BigDecimal vitC;

	@Column(name = "vit_d", precision = 8, scale = 2, nullable = false)
	private BigDecimal vitD;

	@Column(name = "calcium", precision = 8, scale = 2, nullable = false)
	private BigDecimal calcium;

	@Column(name = "iron", precision = 8, scale = 2, nullable = false)
	private BigDecimal iron;

	@Column(name = "sodium", precision = 8, scale = 2, nullable = false)
	private BigDecimal sodium;

	@Column(name = "thumbnail_url", length = 255, nullable = false)
	private String thumbnailUrl;
}
