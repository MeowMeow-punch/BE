package MeowMeowPunch.pickeat.domain.user.entity;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;
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

/**
 * [User][Entity] UserAllergy
 * 사용자와 알러지 간의 다대다 관계를 해소하는 중계 엔티티
 * <p>
 * [Domain Model]
 * 
 * <pre>
 * ┌───────────────┐       ┌───────────────┐
 * │     User      │   N:1 │  UserAllergy  │ 1:N   ┌───────────────┐
 * │   (users)     │ ◀──── │(user_allergies)│ ────▶ │    Allergy    │
 * └───────────────┘       └───────────────┘       │  (allergies)  │
 *                                                 └───────────────┘
 * </pre>
 * </p>
 * - 관계 매핑: 사용자(1) - 알러지(N)
 * - 데이터 모델: 별도의 속성 없이 관계만을 정의함
 */
@Getter
@Entity
@Table(name = "user_allergies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserAllergy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_allergy_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergy allergy;
}
