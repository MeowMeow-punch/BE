package MeowMeowPunch.pickeat.domain.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.global.common.enums.OAuthProvider;

/**
 * [Auth][Repository] UserRepository
 * <p>
 * - JPA Repository 인터페이스를 통해 User 엔티티에 대한 데이터 접근을 추상화
 * - OAuth 식별자로 사용자 조회와 닉네임 중복 체크 기능 제공
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * OAuth 제공자와 ID로 사용자 조회.
	 *
	 * @param oauthProvider OAuth 제공자 (KAKAO, GOOGLE 등)
	 * @param oauthId       OAuth 제공자 측의 사용자 식별자
	 * @return 사용자 엔티티 (Optional)
	 */
	Optional<User> findByOauthProviderAndOauthId(OAuthProvider oauthProvider, String oauthId);

	/**
	 * 닉네임 중복 여부 확인.
	 *
	 * @param nickname확인할 닉네임
	 * @return 중복 시 true, 사용 가능 시 false
	 */
	boolean existsByNickname(String nickname);
}