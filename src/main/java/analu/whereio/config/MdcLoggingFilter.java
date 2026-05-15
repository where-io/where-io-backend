package analu.whereio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID  = "requestId";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String REQUEST_URI = "requestUri";
    private static final String CLIENT_IP   = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put(REQUEST_ID,  UUID.randomUUID().toString());
            MDC.put(HTTP_METHOD, request.getMethod());
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(CLIENT_IP,   resolveClientIp(request));

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
