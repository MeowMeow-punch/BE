package MeowMeowPunch.pickeat.global.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>JwtAuthenticationFilter</h2>
 * <p>
 * Authorization 헤더의 <b>Bearer Token</b>을 추출하여 검증하는 시큐리티 필터.
 * </p>
 *
 * <pre>
 *       Header(Authorization)          Parsing & Verify           SecurityContext
 * ┌──────────────────────────┐   ┌───────────────────────┐   ┌────────────────────┐
 * │ Bearer eyJhbGciOiJI...   │──►│ JwtTokenProvider      │──►│ Authentication     │
 * └──────────────────────────┘   │ (Signature Check)     │   │ (UserPrincipal)    │
 *                                └───────────────────────┘   └────────────────────┘
 *                                            │
 *                                     [Exception]
 *                                            ▼
 *                                   request.setAttribute
 *                                   (EntryPoint에서 401)
 * </pre>
 *
 * @see JwtAuthenticationEntryPoint
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

		try {
			if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
				String token = bearerToken.substring(BEARER_PREFIX.length());
				Claims claims = jwtTokenProvider.parseClaims(token);
				String userId = claims.getSubject();

				// DB 조회 없이 토큰 정보만으로 Principal 생성 (성능 최적화)
				UserPrincipal principal = new UserPrincipal(Long.parseLong(userId));

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						principal,
						null,
						principal.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			log.error("Security Filter Exception: {}", e.getMessage());
			SecurityContextHolder.clearContext();
			request.setAttribute("exception", e);
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		// 모든 요청에 대해 필터를 수행하되, 헤더가 없으면 doFilterInternal에서 자연스럽게 넘어감
		return false;
	}
}