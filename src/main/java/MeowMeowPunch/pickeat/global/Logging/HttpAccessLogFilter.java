package MeowMeowPunch.pickeat.global.Logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.kv;


@Component
@Order(2) // MDC 필터 다음에 실행
public class HttpAccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("HTTP_ACCESS_LOGGER");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            log.info("httpAccess",
                    kv("logType", LogType.HTTP_ACCESS.name()),
                    kv("method", request.getMethod()),
                    kv("uri", request.getRequestURI()),
                    kv("query", request.getQueryString()),
                    kv("status", response.getStatus()),
                    kv("responseTimeMs", duration),
                    kv("clientIp", MDC.get(MdcKeys.CLIENT_IP)),
                    kv("traceId", MDC.get(MdcKeys.TRACE_ID))
            );
        }
    }
}