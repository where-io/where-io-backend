package analu.whereio.config.security;

import analu.whereio.exceptions.handler.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);

    private final byte[] expectedKey;
    private final ObjectMapper objectMapper;

    public InternalApiKeyFilter(@Value("${app.internal.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.expectedKey = apiKey.getBytes();
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String provided = request.getHeader("X-Internal-Key");
        if (provided == null || !MessageDigest.isEqual(provided.getBytes(), expectedKey)) {
            log.warn("Rejected /internal request — missing or invalid X-Internal-Key: {}", request.getRequestURI());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(403, "Acesso negado"));
            return;
        }
        chain.doFilter(request, response);
    }
}
