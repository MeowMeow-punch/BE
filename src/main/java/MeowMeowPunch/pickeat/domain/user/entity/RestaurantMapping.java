package MeowMeowPunch.pickeat.domain.user.entity;

import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [User][Entity] RestaurantMapping
 * 그룹(소속) 정보를 외부 데이터(CSV)와 매핑하기 위한 엔티티
 * <p>
 * [Domain Model]
 * 
 * <pre>
 * ┌───────────────────────┐
 * │   RestaurantMapping   │
 * ├───────────────────────┤
 * │ - id                  │ ← PK
 * │ - restaurantId        │ ← 식별 코드 (CSV ID)
 * │ - restaurantName      │ ← 소속 명칭
 * └───────────────────────┘
 * </pre>
 * </p>
 * - 용도: 소속 검색 및 사용자 그룹 정보 매핑
 * - 데이터 원천: 외부 CSV 데이터를 적재하여 사용
 */
@Getter
@Entity
@Table(name = "restaurant_mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RestaurantMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "restaurant_id", nullable = false, length = 32)
    private String restaurantId;

    @Column(name = "restaurant_name", nullable = false, length = 255)
    private String restaurantName;
}
