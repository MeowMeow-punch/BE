package MeowMeowPunch.pickeat.global.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import MeowMeowPunch.pickeat.global.common.template.ResTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ResTemplate<Void> errorResponse = ResTemplate.error(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다.");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
