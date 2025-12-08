package MeowMeowPunch.pickeat.global.common.dto.response;

import lombok.Builder;

public record PageInfo(
	String nextCursor,
	boolean hasNext
) {
	@Builder
	public PageInfo {
	}
}
