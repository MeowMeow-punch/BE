package MeowMeowPunch.pickeat.domain.diet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [Diet][Scheduler] 시간대별 자동 추천 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DietAutoRecommendationScheduler {

	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

	private final UserRepository userRepository;
	private final DietService dietService;

	/**
	 * 매일 06:00 아침 추천 자동 생성 (없을 때만)
	 */
	@Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
	public void breakfastAutoRecommend() {
		runAutoRecommend(DietType.BREAKFAST);
	}

	/**
	 * 매일 10:00 점심 추천 자동 생성 (없을 때만)
	 */
	@Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
	public void lunchAutoRecommend() {
		runAutoRecommend(DietType.LUNCH);
	}

	/**
	 * 매일 15:00 저녁 추천 자동 생성 (없을 때만)
	 */
	@Scheduled(cron = "0 0 15 * * *", zone = "Asia/Seoul")
	public void dinnerAutoRecommend() {
		runAutoRecommend(DietType.DINNER);
	}

	/**
	 * 매일 21:00 간식 추천 자동 생성 (없을 때만)
	 */
	@Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
	public void snackAutoRecommend() {
		runAutoRecommend(DietType.SNACK);
	}

	private void runAutoRecommend(DietType slot) {
		LocalDate today = LocalDate.now(KOREA_ZONE);
		LocalTime now = LocalTime.now(KOREA_ZONE);
		log.info("[DietAutoRecommendationScheduler] start slot={} at {}", slot, LocalDateTime.of(today, now));
		userRepository.findAll().forEach(user -> {
			try {
				dietService.generateNextRecommendation(user.getId().toString(), slot);
			} catch (Exception e) {
				log.error("[DietAutoRecommendationScheduler] auto recommend failed: userId={}, slot={}",
					user.getId(), slot, e);
			}
		});
	}
}
