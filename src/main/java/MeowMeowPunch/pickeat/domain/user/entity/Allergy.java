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
 * [User][Entity] Allergy
 * 사용자 알러지 정보를 관리하는 불변 도메인 엔티티
 * <p>
 * [Domain Model]
 * 
 * <pre>
 * ┌───────────────┐
 * │    Allergy    │
 * ├───────────────┤
 * │ - id          │ ← PK
 * │ - name        │ ← 알러지명 (Unique)
 * └───────────────┘
 * </pre>
 * </p>
 * - 데이터 성격: 관리자가 정의하는 메타 데이터 (변하지 않음)
 * - 주요 기능: 사용자-알러지 매핑을 위한 참조 대상
 */
@Getter
@Entity
@Table(name = "allergies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Allergy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
