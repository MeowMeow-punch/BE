package MeowMeowPunch.pickeat.global.common.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [Auth][Entity] RefreshToken
 *
 * Redis에 저장되는 리프레시 토큰 정보
 * 사용자 단위로 1개의 토큰만 유지하며 TTL 기반으로 만료를 관리
 *
 * id - 사용자 ID (Redis 키, UUID String)
 * tokenValue - 리프레시 토큰 문자열
 * expiryAt - 만료 시각 (감사/로그용)
 * ttlSeconds - TTL(초) - Redis 자동 만료용
 */
@Getter
@Builder
@RedisHash(value = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken {

	@Id
	private String id;
	private String tokenValue;
	private Instant expiryAt;

	@TimeToLive
	private Long ttlSeconds;

	/**
	 * [Token Rotation] 리프레시 토큰 재발급 시 문자열과 만료 시각을 동시 갱신
	 *
	 * @param updatedToken  새로 발급된 토큰 문자열
	 * @param updatedExpiry 새 만료 시각
	 * @param updatedTtl    새 TTL(초)
	 */
	public void rotate(String updatedToken, Instant updatedExpiry, long updatedTtl) {
		this.tokenValue = updatedToken;
		this.expiryAt = updatedExpiry;
		this.ttlSeconds = updatedTtl;
	}
}