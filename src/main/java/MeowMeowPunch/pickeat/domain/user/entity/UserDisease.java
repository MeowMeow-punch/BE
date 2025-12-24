package MeowMeowPunch.pickeat.domain.user.entity;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
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
 * [User][Entity] UserDisease
 * 사용자와 질환 간의 다대다 관계를 중계하는 엔티티
 * <p>
 * [Domain Model]
 * 
 * <pre>
 * ┌───────────────┐       ┌────────────────┐       ┌───────────────┐
 * │     User      │   N:1 │  UserDisease   │  1:N  │    Disease    │
 * │   (users)     │ ◀──── │(user_diseases) │ ────▶ │  (diseases)   │
 * └───────────────┘       └────────────────┘       └───────────────┘
 * </pre>
 * </p>
 * - 관계 매핑: 사용자(N) - 질환(N)
 * - 데이터 모델: 별도의 속성 없이 관계만을 정의함
 */
@Getter
@Entity
@Table(name = "user_diseases")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserDisease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_disease_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;
}
