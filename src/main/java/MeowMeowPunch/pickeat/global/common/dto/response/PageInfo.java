package MeowMeowPunch.pickeat.global.common.dto.response;

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
