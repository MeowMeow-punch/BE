package MeowMeowPunch.pickeat.global.common.template;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@Getter
@JsonPropertyOrder({ "code", "message", "data" })
public class ResTemplate<T> {
	int code;
	String message;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	T data;

	// Private 생성자 => success, error 메서드로 반환
	private ResTemplate(HttpStatus httpStatus, String message, T data) {
		this.code = httpStatus.value();
		this.message = message;
		this.data = data;
	}

	// 성공
	public static <T> ResTemplate<T> success(HttpStatus httpStatus, String message, T data) {
		return new ResTemplate<>(httpStatus, message, data);
	}

	// 실패
	public static <T> ResTemplate<T> error(HttpStatus httpStatus, String message) {
		return new ResTemplate<>(httpStatus, message, null);
	}
}