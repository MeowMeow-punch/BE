package MeowMeowPunch.pickeat.domain.auth.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import MeowMeowPunch.pickeat.domain.auth.exception.AuthNotFoundException;
import MeowMeowPunch.pickeat.domain.auth.repository.UserRepository;
import MeowMeowPunch.pickeat.global.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;

/**
 * [Security][Service] CustomUserDetailsService
 * JWT의 subject(userId)로 DB에서 사용자를 조회하여 UserDetails 생성.
 * <p>
 * [Interaction Flow]
 * 
 * <pre>
 * [JwtAuthenticationFilter]
 *          │
 *          ▼
 * [CustomUserDetailsService] ─▶ [UserRepository]
 *          │                        │
 *          │                        ▼
 *          ◀─────────────────── [User Entity]
 *          │
 *          ▼
 * [UserPrincipal] (UserDetails)
 * </pre>
 * </p>
 * - 사용처: Spring Security 인증 과정에서 사용자 정보를 로드할 때 사용.
 * - 주의: 현재 JWT 필터 로직이 최적화되어 실제 요청 처리 시에는 호출되지 않을 수 있음.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		UUID uuid;
		try {
			// UUID 형식이 아닌 경우 예외 처리
			uuid = UUID.fromString(userId);
		} catch (IllegalArgumentException e) {
			throw AuthNotFoundException.userNotFound();
		}

		User user = userRepository.findById(Objects.requireNonNull(uuid))
				.orElseThrow(AuthNotFoundException::userNotFound);
		return UserPrincipal.from(user);
	}
}