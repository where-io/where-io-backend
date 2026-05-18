package analu.whereio.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("InternalApiKeyFilter")
class InternalApiKeyFilterTest {

    private InternalApiKeyFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        filter = new InternalApiKeyFilter("test-secret", objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @Nested
    @DisplayName("GET /internal/**")
    class InternalPaths {

        @Test
        @DisplayName("passes through with correct API key")
        void allowsWithCorrectKey() throws Exception {
            request.setRequestURI("/internal/amigos/user123");
            request.addHeader("X-Internal-Key", "test-secret");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("returns 403 with wrong API key")
        void rejectsWithWrongKey() throws Exception {
            request.setRequestURI("/internal/amigos/user123");
            request.addHeader("X-Internal-Key", "wrong-secret");

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(403);
            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("returns 403 when API key header is absent")
        void rejectsWithMissingKey() throws Exception {
            request.setRequestURI("/internal/amigos/user123");

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(403);
            verify(chain, never()).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Non-internal paths")
    class NonInternalPaths {

        @Test
        @DisplayName("skips filter and delegates for /api/** paths")
        void skipsNonInternalPaths() throws Exception {
            request.setRequestURI("/api/amigos");

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }
}
