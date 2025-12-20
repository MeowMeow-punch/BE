package MeowMeowPunch.pickeat.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;

/**
 * [Auth][Repository] UserRepository
 * OAuth 식별자와 닉네임 중복 검사를 담당하는 사용자 저장소.
 * <p>
 * [Role]
 * - OAuth Login: findByOauthProviderAndOauthId
 * - Duplicate Check: existsByNickname
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByOauthProviderAndOauthId(OAuthProvider oauthProvider, String oauthId);

	boolean existsByNickname(String nickname);
}