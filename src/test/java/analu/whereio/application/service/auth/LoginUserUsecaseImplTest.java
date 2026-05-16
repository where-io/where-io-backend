package analu.whereio.application.service.auth;

import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("POST /api/auth/login - LoginUserUsecaseImpl")
class LoginUserUsecaseImplTest {

    @Mock
    private UserAccountRepositoryPort userAccountRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenIssuerService authTokenIssuerService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private LoginUserUsecaseImpl loginUserUsecaseImpl;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        user = new UserAccount();
        user.setId("user-id-1");
        user.setEmail("user@example.com");
        user.setEncodedPassword("hashed-password");

        lenient().when(loginAttemptService.getLockoutRemaining(anyString()))
                .thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("quando a conta está bloqueada")
    class QuandoContaBloqueada {

        @Test
        @DisplayName("deve lançar BusinessException 429 com tempo restante na mensagem")
        void deveLancar429ComTempoRestante() {
            when(loginAttemptService.getLockoutRemaining("user@example.com"))
                    .thenReturn(Optional.of(Duration.ofMinutes(9).plusSeconds(30)));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> loginUserUsecaseImpl.execute("user@example.com", "qualquer-senha"));

            assertAll(
                    () -> assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatus()),
                    () -> assertTrue(ex.getMessage().contains("9 min e 30 seg"))
            );
            verify(userAccountRepositoryPort, never()).findByEmail(any());
        }
    }

    @Nested
    @DisplayName("quando as credenciais são inválidas")
    class QuandoCredenciaisInvalidas {

        @Test
        @DisplayName("deve registrar falha quando a senha está incorreta")
        void deveRegistrarFalhaComSenhaIncorreta() {
            when(userAccountRepositoryPort.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha-errada", "hashed-password")).thenReturn(false);

            assertThrows(BusinessException.class,
                    () -> loginUserUsecaseImpl.execute("user@example.com", "senha-errada"));

            verify(loginAttemptService).recordFailure("user@example.com");
        }

        @Test
        @DisplayName("deve registrar falha quando a senha é nula")
        void deveRegistrarFalhaComSenhaNula() {
            when(userAccountRepositoryPort.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));

            assertThrows(BusinessException.class,
                    () -> loginUserUsecaseImpl.execute("user@example.com", null));

            verify(loginAttemptService).recordFailure("user@example.com");
        }

        @Test
        @DisplayName("não deve registrar falha quando o e-mail não existe")
        void naoDeveRegistrarFalhaComEmailInexistente() {
            when(userAccountRepositoryPort.findByEmail("naoexiste@example.com"))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class,
                    () -> loginUserUsecaseImpl.execute("naoexiste@example.com", "qualquer-senha"));

            verify(loginAttemptService, never()).recordFailure(any());
        }
    }

    @Nested
    @DisplayName("quando o login é bem-sucedido")
    class QuandoLoginBemSucedido {

        @Test
        @DisplayName("deve registrar sucesso e retornar tokens")
        void deveRegistrarSucessoERetornarTokens() {
            AuthTokens tokens = new AuthTokens("access-token", "refresh-token", 3600L);
            when(userAccountRepositoryPort.findByEmail("user@example.com"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha-correta", "hashed-password")).thenReturn(true);
            when(authTokenIssuerService.issueForUser(user)).thenReturn(tokens);

            AuthTokens result = loginUserUsecaseImpl.execute("user@example.com", "senha-correta");

            assertAll(
                    () -> assertEquals("access-token", result.accessToken()),
                    () -> assertEquals("refresh-token", result.refreshToken())
            );
            verify(loginAttemptService).recordSuccess("user@example.com");
            verify(loginAttemptService, never()).recordFailure(any());
        }
    }
}
