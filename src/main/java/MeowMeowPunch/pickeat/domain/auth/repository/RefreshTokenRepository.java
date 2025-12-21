package MeowMeowPunch.pickeat.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import MeowMeowPunch.pickeat.global.common.entity.RefreshToken;

/**
 * [Auth][Repository] RefreshTokenRepository
 * <p>
 * - JPA CrudRepository 인터페이스를 통해 RefreshToken 엔티티에 대한 데이터 접근을 추상화
 * - Redis에 저장된 RefreshToken을 조회하고 삭제하는 기능 제공
 * </p>
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}