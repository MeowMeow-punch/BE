package MeowMeowPunch.pickeat.domain.user.dto.response;

import MeowMeowPunch.pickeat.welstory.entity.GroupMapping;

/**
 * [User][DTO] 소속(그룹) 검색 결과 응답 DTO.
 * <p>
 * - groupId: 그룹 식별자 (PK)
 * - groupName: 그룹명
 * </p>
 */
public record UserGroupResponse(
        Long groupId,
        String groupName) {
    /**
     * GroupMapping 엔티티를 UserGroupResponse DTO로 변환.
     *
     * @param groupMapping 변환할 그룹 엔티티
     * @return 변환된 DTO
     */
    public static UserGroupResponse from(GroupMapping groupMapping) {
        return new UserGroupResponse(
                groupMapping.getId(),
                groupMapping.getGroupName());
    }
}
