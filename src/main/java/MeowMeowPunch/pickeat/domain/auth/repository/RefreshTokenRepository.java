package MeowMeowPunch.pickeat.domain.auth.repository;

import org.springframework.data.repository.CrudRepository;

import MeowMeowPunch.pickeat.global.common.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}