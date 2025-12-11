package MeowMeowPunch.pickeat.global.common.dto.response;

// 커서 기반 페이지네이션 정보를 보내주는 공통 DTO
public record PageInfo(
	String nextCursor,
	boolean hasNext
) {
	public static PageInfo of(String nextCursor, boolean hasNext) {
		return new PageInfo(nextCursor, hasNext);
	}

	public static PageInfo empty() {
		return new PageInfo(null, false);
	}
}
