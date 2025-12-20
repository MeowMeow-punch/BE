package MeowMeowPunch.pickeat.domain.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByOauthProviderAndOauthId(OAuthProvider oauthProvider, String oauthId);

	boolean existsByNickname(String nickname);
}