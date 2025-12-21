package MeowMeowPunch.pickeat.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.exception.AuthNotFoundException;
import MeowMeowPunch.pickeat.domain.auth.exception.DuplicateNicknameException;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.domain.diet.repository.DietRepository;
import MeowMeowPunch.pickeat.domain.user.dto.request.FocusUpdateRequest;
import MeowMeowPunch.pickeat.domain.user.dto.request.UserUpdateRequest;
import MeowMeowPunch.pickeat.domain.user.dto.response.MyPageResponse;
import MeowMeowPunch.pickeat.domain.user.dto.response.UserGroupResponse;
import MeowMeowPunch.pickeat.domain.user.exception.InvalidKeywordException;
import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;
import MeowMeowPunch.pickeat.welstory.repository.GroupMappingRepository;

/**
 * [User][Service] 사용자 도메인 비즈니스 로직 처리.
 * 닉네임 중복 확인, 소속 검색 등 가입 전/후 사용자 관련 기능을 담당.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final GroupMappingRepository groupMappingRepository;
    private final DietRepository dietRepository;

    /**
     * 닉네임 중복 여부를 확인합니다.
     * <p>
     * 입력된 닉네임이 이미 DB에 존재하는지 검사하며,
     * 존재할 경우 {@link DuplicateNicknameException} 예외를 던집니다.
     * </p>
     *
     * @param nickname 검증할 닉네임
     * @throws DuplicateNicknameException 닉네임이 이미 존재하는 경우 (409 Conflict)
     */
    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException("해당 닉네임은 사용중입니다.");
        }
    }

    /**
     * 키워드를 사용하여 소속(그룹)을 검색합니다.
     * <p>
     * 그룹명에 해당 키워드가 포함된 모든 그룹을 조회합니다 (Like 검색).
     * 검색 결과는 {@link UserGroupResponse} DTO 리스트로 변환되어 반환됩니다.
     * </p>
     *
     * @param keyword 검색할 그룹명 키워드 (2글자 이상)
     * @return 검색된 그룹 목록 (없을 경우 빈 리스트 반환)
     * @throws InvalidKeywordException 검색어가 유효하지 않은 경우 (null, 빈 값, 2글자 미만) - 400 Bad
     *                                 Request
     */
    public List<UserGroupResponse> searchGroups(String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            throw InvalidKeywordException.tooShort();
        }

        List<GroupMapping> groups = groupMappingRepository.findByGroupNameContaining(keyword);
        return groups.stream()
                .map(UserGroupResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 마이페이지 정보를 조회합니다.
     * <p>
     * - 사용자 프로필 (닉네임, 소속 등)
     * - 활동 요약 (스트릭, 주간 식단 기록)
     * - 건강/신체 정보 (키, 몸무게, 알러지 등)
     * </p>
     *
     * @param userId 사용자 식별자 (UUID)
     * @return 마이페이지 응답 DTO
     */
    public MyPageResponse getMyPage(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthNotFoundException::userNotFound);

        // 1. 그룹(웰스토리 회사명) 조회
        String groupName = null;
        if (user.getGroupId() != null) {
            groupName = groupMappingRepository.findById(user.getGroupId())
                    .map(GroupMapping::getGroupName)
                    .orElse(null);
        }

        // 2. Activity Summary - 스트릭 날짜 계산
        long totalRecordedDays = dietRepository.countByUserId(userId.toString());
        List<LocalDate> distinctDates = dietRepository.findDistinctDatesByUserId(userId.toString());
        int currentStreak = calculateCurrentStreak(distinctDates);

        // 3. Activity Summary - 주간 식단 기록
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        long weeklyRecordedCount = dietRepository.countByUserIdAndDateBetween(
                userId.toString(), startOfWeek, endOfWeek);
        int weeklyTargetCount = user.getMeals().getCount() * 7; // 사용자가 먹는 끼니 * 7

        MyPageResponse.UserProfile userProfile = MyPageResponse.UserProfile.from(user, groupName);

        MyPageResponse.ActivitySummary activitySummary = MyPageResponse.ActivitySummary.of(
                currentStreak,
                totalRecordedDays,
                weeklyRecordedCount,
                weeklyTargetCount);

        MyPageResponse.BasicInfo basicInfo = MyPageResponse.BasicInfo.from(user);

        return MyPageResponse.of(userProfile, activitySummary, basicInfo);
    }

    private int calculateCurrentStreak(List<LocalDate> dates) {
        if (dates.isEmpty())
            return 0;

        int streak = 0;
        LocalDate checkDate = LocalDate.now();

        LocalDate latest = dates.get(0);
        // 가장 최근 기록이 오늘 또는 어제여야 스트릭이 유효 (연속성 판단 기준)
        if (!latest.equals(checkDate) && !latest.equals(checkDate.minusDays(1))) {
            return 0;
        }

        LocalDate current = latest;

        for (LocalDate date : dates) {
            if (date.equals(current)) {
                streak++;
                current = current.minusDays(1);
            } else {
                break; // 연속되지 않음
            }
        }
        return streak;
    }

    /**
     * 사용자 정보를 수정합니다.
     * <p>
     * - 닉네임 변경 시 중복 체크 수행
     * - 각 필드가 null이 아닌 경우에만 수정
     * </p>
     *
     * @param userId  사용자 식별자
     * @param request 수정할 정보
     */
    @Transactional
    public void updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthNotFoundException::userNotFound);

        // 닉네임 변경 시 중복 체크
        if (request.nickname() != null && !request.nickname().equals(user.getNickname())) {
            checkNickname(request.nickname());
        }

        Long parsedGroupId = null;
        if (request.groupId() != null) {
            try {
                parsedGroupId = Long.parseLong(request.groupId());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("잘못된 그룹 ID 형식입니다.");
            }
        }

        user.updateProfile(
                request.nickname(),
                parsedGroupId,
                request.gender(),
                request.height(),
                request.weight(),
                request.age(),
                request.allergies(),
                request.isMarketing());

        // save 없어도 변경 감지로 자동 저장됨미다
    }

    /**
     * 식단 중점 목표(Focus) 및 관련 정보를 수정합니다.
     *
     * @param userId  사용자 식별자
     * @param request 수정할 정보 (Focus 필수)
     */
    @Transactional
    public void updateDietFocus(UUID userId, FocusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthNotFoundException::userNotFound);

        user.updateDietFocus(
                request.focus(),
                request.meals(),
                request.activityLevel(),
                request.targetWeight(),
                request.diseases(),
                request.isSmoking(),
                request.isDrinking());

        // save 없어도 변경 감지로 자동 저장됨미다
    }
}
