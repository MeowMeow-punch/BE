package MeowMeowPunch.pickeat.domain.auth.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import MeowMeowPunch.pickeat.global.common.enums.ActivityLevel;
import MeowMeowPunch.pickeat.global.common.enums.DrinkingStatus;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.Gender;
import MeowMeowPunch.pickeat.global.common.enums.MealFrequency;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;
import MeowMeowPunch.pickeat.global.common.enums.SmokingStatus;
import MeowMeowPunch.pickeat.global.common.enums.UserStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [Auth][Entity] User
 * 소셜 OAuth 식별자와 식습관 프로필을 저장하는 핵심 사용자 엔티티
 * <p>
 * [Domain Model]
 * 
 * <pre>
 * ┌───────────────┐
 * │     User      │
 * ├───────────────┤
 * │ - oauthId     │ ← Account Key
 * │ - nickname    │ ← Display Name
 * │ - status      │ ← SINGLE / GROUP
 * │ - focus       │ ← DIET / BULK_UP / BALANCE
 * │ - profile...  │ ← Height, Weight, Age...
 * └───────────────┘
 * </pre>
 * </p>
 * - 보안 키워드: OAuthProvider + oauthId 복합키로 계정 식별
 * - 데이터 모델: 식단 추천을 위한 건강/생활 패턴 속성 포함
 * - 감사 로그: createdAt/updatedAt을 통해 추후 변경 이력 트래킹 가능
 */
import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

	/**
	 * [Identity] UUID v7 Strategy
	 * <p>
	 * - <b>GenerationType.UUID</b>: 기본적으로 무작위(v4) 방식을 사용하면 DB Insert 성능 저하(Index
	 * Fragmentation) 문제가 발생함 그래서 단순 GenerationType은 사용 안함
	 * - <b>UuidGenerator (Time-based)</b>: 이를 해결하기 위해 타임스탬프가 포함된 <b>UUID v7</b> 방식을
	 * 적용.
	 * - 효과: 생성 시간순 정렬이 보장되어 Clustered Index 성능이 최적화된다 카드라..!!!
	 * </p>
	 */
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OAuthProvider oauthProvider;

	@Column(nullable = false, length = 128)
	private String oauthId;

	@Column(nullable = false, unique = true, length = 50)
	private String nickname;

	@Column(nullable = false)
	private boolean marketingAgreed;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Gender gender;

	private Integer height;

	private Integer weight;

	private Integer age;

	@ElementCollection
	@CollectionTable(name = "user_allergies_legacy", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "allergy", length = 100)
	@Builder.Default
	private List<String> allergies = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "user_diseases_legacy", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "disease", length = 100)
	@Builder.Default
	private List<String> diseases = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private UserStatus status;

	private Long groupId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Focus focus;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private SmokingStatus smokingStatus;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private DrinkingStatus drinkingStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private MealFrequency meals;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private ActivityLevel activityLevel;

	private Integer targetWeight;

	// --- Business Logic Methods ---

	/**
	 * 프로필 정보를 업데이트합니다.
	 * null이 아닌 값만 변경됩니다.
	 */
	public void updateProfile(String nickname, Long groupId, Gender gender,
			Integer height, Integer weight, Integer age,
			List<String> allergies, Boolean marketingAgreed) {
		if (nickname != null)
			this.nickname = nickname;
		if (groupId != null) {
			this.groupId = groupId;
		}
		if (gender != null)
			this.gender = gender;
		if (height != null)
			this.height = height;
		if (weight != null)
			this.weight = weight;
		if (age != null)
			this.age = age;
		if (allergies != null) {
			this.allergies.clear();
			this.allergies.addAll(allergies);
		}
		if (marketingAgreed != null)
			this.marketingAgreed = marketingAgreed;
	}
}