package MeowMeowPunch.pickeat.domain.user.dto.request;

import java.util.List;

import MeowMeowPunch.pickeat.global.common.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * [User][DTO] UserUpdateRequest
 * 개인정보 수정 요청 DTO
 * - 모든 필드는 Optional (null일 경우 수정하지 않음)
 */
public record UserUpdateRequest(
                @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.") @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.") String nickname,

                // GroupID는 숫자 형식이지만 String으로 전달됨 -> Service에서 파싱 검증
                String groupId,

                Gender gender,

                @Min(value = 0, message = "키는 0 이상이어야 합니다.") @Max(value = 300, message = "키는 300 이하여야 합니다.") Integer height,

                @Min(value = 0, message = "몸무게는 0 이상이어야 합니다.") @Max(value = 500, message = "몸무게는 500 이하여야 합니다.") Integer weight,

                @Min(value = 0, message = "나이는 0 이상이어야 합니다.") @Max(value = 150, message = "나이는 150 이하여야 합니다.") Integer age,

                List<String> allergies,

                Boolean isMarketing) {
}
