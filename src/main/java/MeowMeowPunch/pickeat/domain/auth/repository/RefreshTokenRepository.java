package MeowMeowPunch.pickeat.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;

import MeowMeowPunch.pickeat.global.common.entity.RefreshToken;

/**
 * [Auth][Repository] RefreshTokenRepository
 * 리프레시 토큰의 발급/회수/회전을 담당하는 저장소.
 * <p>
 * [Features]
 * - Key: UserId (Long)
 * - Value: RefreshToken (Redis Hash / Database Entity)
 * - TTL: 토큰 만료 시간과 동일하게 설정됨
 * </p>
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

}