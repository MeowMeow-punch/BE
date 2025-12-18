package MeowMeowPunch.pickeat.global.jwt;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <h2>JwtAuthenticationEntryPoint</h2>
 * <p>
 * 인증되지 않은 사용자(Anonymous)가 보호된 리소스에 접근할 때 호출되는 핸들러.<br>
 * 401 Unauthorized 상태 코드와 함께 표준화된 에러 응답(JSON)을 반환한다.
 * </p>
 *
 * <pre>
 *      Filter Chain             Client
 *           │ 401 Error           ▲
 *           ▼                     │
 *    [Entry Point] ───────────────┘
 *     JSON Response
 * </pre>
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        // try-catch로 잡힌 예외가 속성에 있다면 로그를 남기거나 메시지를 구체화할 수 있음
        // Exception exception = (Exception) request.getAttribute("exception");

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\": \"AUTH_001\", \"message\": \"인증에 실패했습니다.\"}");
    }
}
