package MeowMeowPunch.pickeat.domain.auth.dto.request;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.ActivityLevel;
import MeowMeowPunch.pickeat.global.common.enums.DrinkingStatus;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.Gender;
import MeowMeowPunch.pickeat.global.common.enums.MealFrequency;

import MeowMeowPunch.pickeat.global.common.enums.SmokingStatus;
import MeowMeowPunch.pickeat.global.common.enums.UserStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * [Auth][DTO] SignUpRequest
 *
 * 회원가입 시 건강/생활 프로필을 수집하는 요청 레코드.
 * <p>
 * [Data Group]
 * - Account: oauthProvider, oauthId, nickname
 * - Profile: gender, height, weight, age
 * - Health: allergies, diseases, smoking, drinking
 * - Diet: status(SINGLE/GROUP), focus, meals, activityLevel
 * </p>
 * - 정합성 키워드: 닉네임 중복 검사, 그룹 ID 존재 여부
 * - 보안 키워드: OAuthProvider와 oauthId로 계정 연결
 */
public record SignUpRequest(
		@NotBlank(message = "registerToken은 필수입니다.") String registerToken,
		@NotBlank(message = "닉네임은 필수입니다.") String nickname,
		@NotNull(message = "마케팅 수신 동의 여부를 입력해주세요.") Boolean isMarketing,
		@NotNull(message = "성별은 필수입니다.") Gender gender,
		@Positive(message = "키는 0보다 커야 합니다.") Integer height,
		@Positive(message = "몸무게는 0보다 커야 합니다.") Integer weight,
		@Min(value = 1, message = "나이는 1 이상이어야 합니다.") Integer age,
		List<String> allergies,
		List<String> diseases,
		@NotNull(message = "사용자 구분은 필수입니다.") UserStatus status,
		String groupId,
		@NotNull(message = "중점 목표는 필수입니다.") Focus focus,
		SmokingStatus smokingStatus,
		DrinkingStatus drinkingStatus,
		@NotNull(message = "하루 식사 횟수는 필수입니다.") MealFrequency meals,
		@NotNull(message = "활동량은 필수입니다.") ActivityLevel activityLevel,
		@Positive(message = "목표 체중은 0보다 커야 합니다.") Integer targetWeight) {
}