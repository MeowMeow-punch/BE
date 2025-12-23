package MeowMeowPunch.pickeat.domain.community.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityLikeRequest {

	@NotNull(message = "isLiked 값은 필수입니다.")
	private Boolean isLiked;

	public CommunityLikeRequest(Boolean isLiked) {
		this.isLiked = isLiked;
	}
}
