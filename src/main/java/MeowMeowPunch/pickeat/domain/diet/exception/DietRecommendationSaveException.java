package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InternalServerErrorGroupException;

// 추천 식단 저장에 실패했을 때 사용하는 예외
public class DietRecommendationSaveException extends InternalServerErrorGroupException {
	public DietRecommendationSaveException(Throwable cause) {
		super("추천 식단 저장에 실패했습니다.", cause);
	}
}
