package MeowMeowPunch.pickeat.domain.diet.dto.response;

// /diet 응답의 공통 상위 타입 (홈/일자별 조회 모두 사용)
public sealed interface DietResponse permits DietHomeResponse, DailyDietResponse {
}
