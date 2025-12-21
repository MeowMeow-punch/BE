package MeowMeowPunch.pickeat.domain.user.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.global.common.enums.Focus;
import MeowMeowPunch.pickeat.global.common.enums.Gender;

/**
 * [User][DTO] MyPageResponse
 * 마이페이지 조회 결과 응답 DTO (Inner Record 구조)
 */
@Builder
public record MyPageResponse(
                UserProfile userProfile,
                ActivitySummary activitySummary,
                BasicInfo basicInfo) {

        public static MyPageResponse of(UserProfile userProfile, ActivitySummary activitySummary, BasicInfo basicInfo) {
                return MyPageResponse.builder()
                                .userProfile(userProfile)
                                .activitySummary(activitySummary)
                                .basicInfo(basicInfo)
                                .build();
        }

        // 아직 아래 record들은 타 response에 쓰임새가 없기에 따로 분리하진 않았음
        // => 공통되는 response가 생길 경우 분리 예정
        @Builder
        public record UserProfile(
                        UUID userId,
                        String nickname,
                        Focus focus,
                        String groupName,
                        LocalDate createAt) {

                public static UserProfile from(User user, String groupName) {
                        return UserProfile.builder()
                                        .userId(user.getId())
                                        .nickname(user.getNickname())
                                        .focus(user.getFocus())
                                        .groupName(groupName)
                                        .createAt(user.getCreatedAt().toLocalDate())
                                        .build();
                }
        }

        public record ActivitySummary(
                        Streak streak,
                        WeeklyDiet weeklyDiet) {

                public static ActivitySummary of(int currentStreak, long totalRecordedDays, long weeklyRecordedCount,
                                int weeklyTargetCount) {
                        return new ActivitySummary(
                                        new Streak(currentStreak, totalRecordedDays),
                                        new WeeklyDiet(weeklyRecordedCount, weeklyTargetCount));
                }

                public record Streak(
                                int currentDays,
                                long totalRecordedDays) {
                }

                public record WeeklyDiet(
                                long recordedCount,
                                int targetCount) {
                }
        }

        @Builder
        public record BasicInfo(
                        Gender gender,
                        Integer age,
                        Integer weight,
                        Integer height,
                        List<String> allergies) {

                public static BasicInfo from(User user) {
                        return BasicInfo.builder()
                                        .gender(user.getGender())
                                        .age(user.getAge())
                                        .weight(user.getWeight())
                                        .height(user.getHeight())
                                        .allergies(user.getAllergies()) // Legacy field
                                        .build();
                }
        }
}
