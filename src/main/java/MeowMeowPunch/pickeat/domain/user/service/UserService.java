package MeowMeowPunch.pickeat.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import MeowMeowPunch.pickeat.domain.auth.exception.DuplicateNicknameException;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
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
}
