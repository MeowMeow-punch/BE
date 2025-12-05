package MeowMeowPunch.pickeat.global.Logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
public class LoggingContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = Optional.ofNullable(request.getHeader("X-TRACE-ID"))
                    .orElse(UUID.randomUUID().toString());
            MDC.put(MdcKeys.TRACE_ID, traceId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put(MdcKeys.USER_ID, authentication.getName());
            }

            String clientIp = clientIp(request);
            MDC.put(MdcKeys.CLIENT_IP, clientIp);

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private String clientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
