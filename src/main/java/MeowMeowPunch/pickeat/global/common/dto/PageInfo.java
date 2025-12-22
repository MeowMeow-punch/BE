package MeowMeowPunch.pickeat.global.common.dto;

/**
 * [Global][DTO] PageInfo
 * 커서 페이징(Cursor Pagination)을 위한 메타데이터
 *
 * @param nextCursor 다음 페이지 조회를 위한 커서 (null이면 다음 페이지 없음)
 * @param hasNext    다음 페이지 존재 여부
 */
public record PageInfo(
	String nextCursor,
	boolean hasNext
) {
}
