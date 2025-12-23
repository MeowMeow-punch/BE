package MeowMeowPunch.pickeat.domain.community.dto.request;

import MeowMeowPunch.pickeat.global.common.enums.CommunityCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommunityRequest(
	@NotNull(message = "카테고리는 필수입니다.")
	CommunityCategory category,

	@NotBlank(message = "읽는 시간은 필수입니다.")
	@Size(max = 5, message = "읽는 시간은 5자 이내여야 합니다.")
	String readingTime,

	@NotBlank(message = "제목은 필수입니다.")
	@Size(max = 30, message = "제목은 30자 이내여야 합니다.")
	String title,

	@NotBlank(message = "미리보기 텍스트는 필수입니다.")
	@Size(max = 50, message = "미리보기 텍스트는 50자 이내여야 합니다.")
	String previewText,

	@NotBlank(message = "작성자는 필수입니다.")
	@Size(max = 10, message = "작성자는 10자 이내여야 합니다.")
	String writer,

	@NotBlank(message = "썸네일 URL은 필수입니다.")
	String thumbnailUrl,

	@NotBlank(message = "내용은 필수입니다.")
	String content
) {
}
