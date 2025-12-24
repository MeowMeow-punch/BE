package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InternalServerErrorGroupException;

// 일일 AI 피드백 생성 실패 예외
public class DietFeedbackGenerateException extends InternalServerErrorGroupException {
	public DietFeedbackGenerateException(String message, Throwable cause) {
		super(message, cause);
	}
}
