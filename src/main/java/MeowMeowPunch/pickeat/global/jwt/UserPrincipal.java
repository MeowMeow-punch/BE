package MeowMeowPunch.pickeat.global.jwt;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import MeowMeowPunch.pickeat.domain.auth.entity.User;
import lombok.Getter;

/**
 * <h2>UserPrincipal</h2>
 * <p>
 * Spring Security의 {@link UserDetails} 인터페이스를 구현한 클래스.<br>
 * JWT 토큰에서 추출한 사용자 식별자(userId)를 기반으로 인증
 * 객체({@link org.springframework.security.core.Authentication})를 생성하는 어댑터 역할을
 * 한다.
 * </p>
 *
 * <pre>
 *    JWT Claims (Subject)       Spring Security
 *   ┌────────────────────┐    ┌─────────────────┐
 *   │ userId: 12345      │──► │ UserDetails     │
 *   └────────────────────┘    │ (Authenticated) │
 *                             └─────────────────┘
 * </pre>
 *
 * <ul>
 * <li><b>password</b> : OAuth/JWT 기반이므로 빈 문자열 반환</li>
 * <li><b>authorities</b> : 현재 단일 권한(ROLE_USER)으로 고정</li>
 * </ul>
 */
@Getter
public class UserPrincipal implements UserDetails {

	private final UUID userId;

	public UserPrincipal(UUID userId) {
		this.userId = userId;
	}

	public static UserPrincipal from(User user) {
		return new UserPrincipal(user.getId());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return ""; // OAuth 기반 로그인이라 비밀번호 미사용
	}

	@Override
	public String getUsername() {
		return userId.toString();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}