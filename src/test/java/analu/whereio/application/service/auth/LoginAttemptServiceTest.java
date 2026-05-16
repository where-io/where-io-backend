package analu.whereio.application.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptService")
class LoginAttemptServiceTest {

    @Mock
    private Clock clock;

    private LoginAttemptService service;

    private static final String EMAIL = "user@example.com";
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(T0);
        service = new LoginAttemptService(clock);
    }

    @Nested
    @DisplayName("recordFailure")
    class RecordFailure {

        @Test
        @DisplayName("deve retornar vazio com menos de 3 tentativas")
        void deveRetornarVazioAntesDoLimite() {
            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);

            assertTrue(service.getLockoutRemaining(EMAIL).isEmpty());
        }

        @Test
        @DisplayName("deve bloquear na terceira tentativa")
        void deveBloquerNaTerceiraTentativa() {
            when(clock.instant()).thenReturn(T0);

            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);

            Optional<Duration> remaining = service.getLockoutRemaining(EMAIL);

            assertTrue(remaining.isPresent());
            assertEquals(Duration.ofMinutes(10), remaining.get());
        }

        @Test
        @DisplayName("deve manter bloqueio com mais tentativas além do limite e não estender o prazo")
        void deveManterBloqueioAposLimite() {
            when(clock.instant()).thenReturn(T0);

            for (int i = 0; i < 5; i++) service.recordFailure(EMAIL);

            Optional<Duration> remaining = service.getLockoutRemaining(EMAIL);
            assertTrue(remaining.isPresent());
            assertEquals(Duration.ofMinutes(10), remaining.get());
        }
    }

    @Nested
    @DisplayName("recordSuccess")
    class RecordSuccess {

        @Test
        @DisplayName("deve remover bloqueio após login bem-sucedido")
        void deveRemoverBloqueio() {
            when(clock.instant()).thenReturn(T0);

            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);
            service.recordSuccess(EMAIL);

            assertTrue(service.getLockoutRemaining(EMAIL).isEmpty());
        }

        @Test
        @DisplayName("não deve lançar exceção para email sem tentativas registradas")
        void naoDeveLancarExcecaoSemTentativas() {
            assertDoesNotThrow(() -> service.recordSuccess(EMAIL));
        }
    }

    @Nested
    @DisplayName("getLockoutRemaining")
    class GetLockoutRemaining {

        @Test
        @DisplayName("deve retornar vazio para email sem tentativas")
        void deveRetornarVazioSemTentativas() {
            assertTrue(service.getLockoutRemaining(EMAIL).isEmpty());
        }

        @Test
        @DisplayName("deve retornar vazio quando lockout expirou")
        void deveRetornarVazioQuandoLockoutExpirou() {
            // 3ª recordFailure chama clock.instant() uma vez → lockedUntil = T0 + 10min
            // getLockoutRemaining chama clock.instant() uma vez → T0+11min > lockedUntil → expirado
            when(clock.instant())
                    .thenReturn(T0)
                    .thenReturn(T0.plusSeconds(660));

            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);
            service.recordFailure(EMAIL);

            Optional<Duration> remaining = service.getLockoutRemaining(EMAIL);

            assertTrue(remaining.isEmpty());
        }
    }
}
