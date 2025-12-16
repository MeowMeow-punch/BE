package MeowMeowPunch.pickeat.welstory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;

// Welstory 식당 이름 ↔ 식당 ID 매핑 엔티티
@Getter
@Entity
@Table(name = "restaurant_mapping", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"restaurant_id"}),
	@UniqueConstraint(columnNames = {"restaurant_name"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantMapping extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "restaurant_id", nullable = false, length = 32)
	private String restaurantId;

	@Column(name = "restaurant_name", nullable = false, length = 255)
	private String restaurantName;
}
