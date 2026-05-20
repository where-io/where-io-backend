# Login Lockout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bloquear a conta de um usuário por 10 minutos após 3 tentativas de senha incorretas, retornando HTTP 429 com o tempo restante.

**Architecture:** Um `LoginAttemptService` singleton mantém um `ConcurrentHashMap<email, AttemptRecord>` em memória. O `LoginUserUsecaseImpl` consulta esse serviço antes de autenticar (checando lockout) e notifica após cada tentativa (falha ou sucesso). O estado não persiste entre restarts do servidor — decisão deliberada.

**Tech Stack:** Java 17, Spring Boot, JUnit 5, Mockito

---

## File Structure

| Ação | Arquivo | Responsabilidade |
|------|---------|-----------------|
| Create | `src/main/java/analu/whereio/config/AppConfig.java` | Bean `Clock.systemUTC()` para injeção |
| Create | `src/main/java/analu/whereio/application/service/auth/LoginAttemptService.java` | Estado de tentativas + lockout em memória |
| Modify | `src/main/java/analu/whereio/application/service/auth/LoginUserUsecaseImpl.java` | Integrar lockout check/record no fluxo de login |
| Create | `src/test/java/analu/whereio/application/service/auth/LoginAttemptServiceTest.java` | Testes unitários do LoginAttemptService |
| Create | `src/test/java/analu/whereio/application/service/auth/LoginUserUsecaseImplTest.java` | Testes unitários do LoginUserUsecaseImpl modificado |

---

## Task 1: Adicionar bean Clock ao contexto Spring

**Files:**
- Create: `src/main/java/analu/whereio/config/AppConfig.java`

- [ ] **Step 1: Criar `AppConfig.java`**

```java
package analu.whereio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
```

- [ ] **Step 2: Verificar que a aplicação compila**

```bash
./mvnw compile -q
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/analu/whereio/config/AppConfig.java
git commit -m "config: add Clock bean for testable time injection"
```

---

## Task 2: LoginAttemptService — TDD

**Files:**
- Create: `src/test/java/analu/whereio/application/service/auth/LoginAttemptServiceTest.java`
- Create: `src/main/java/analu/whereio/application/service/auth/LoginAttemptService.java`

- [ ] **Step 1: Escrever os testes (classe ainda não existe — vai falhar)**

Criar `src/test/java/analu/whereio/application/service/auth/LoginAttemptServiceTest.java`:

```java
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
        @DisplayName("deve manter bloqueio com mais tentativas além do limite")
        void deveManterBloqueioAposLimite() {
            when(clock.instant()).thenReturn(T0);

            for (int i = 0; i < 5; i++) service.recordFailure(EMAIL);

            assertTrue(service.getLockoutRemaining(EMAIL).isPresent());
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
```

- [ ] **Step 2: Executar os testes para confirmar falha**

```bash
./mvnw test -Dtest=LoginAttemptServiceTest
```

Esperado: ERRO de compilação — `LoginAttemptService` não existe.

- [ ] **Step 3: Criar `LoginAttemptService.java`**

Criar `src/main/java/analu/whereio/application/service/auth/LoginAttemptService.java`:

```java
package analu.whereio.application.service.auth;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    static final int MAX_ATTEMPTS = 3;
    static final Duration LOCKOUT_DURATION = Duration.ofMinutes(10);

    private final Clock clock;
    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void recordFailure(String email) {
        attempts.compute(email, (key, record) -> {
            int newCount = (record == null ? 0 : record.count) + 1;
            Instant lockedUntil = newCount >= MAX_ATTEMPTS
                    ? Instant.now(clock).plus(LOCKOUT_DURATION)
                    : (record != null ? record.lockedUntil : null);
            return new AttemptRecord(newCount, lockedUntil);
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(email);
    }

    public Optional<Duration> getLockoutRemaining(String email) {
        AttemptRecord record = attempts.get(email);
        if (record == null || record.lockedUntil == null) {
            return Optional.empty();
        }
        Duration remaining = Duration.between(Instant.now(clock), record.lockedUntil);
        if (remaining.isNegative() || remaining.isZero()) {
            attempts.remove(email);
            return Optional.empty();
        }
        return Optional.of(remaining);
    }

    record AttemptRecord(int count, Instant lockedUntil) {}
}
```

- [ ] **Step 4: Executar os testes para confirmar que passam**

```bash
./mvnw test -Dtest=LoginAttemptServiceTest
```

Esperado: `BUILD SUCCESS`, todos os testes passando.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/analu/whereio/application/service/auth/LoginAttemptService.java \
        src/test/java/analu/whereio/application/service/auth/LoginAttemptServiceTest.java
git commit -m "feat: add LoginAttemptService for in-memory login lockout tracking"
```

---

## Task 3: Integrar LoginAttemptService no LoginUserUsecaseImpl — TDD

**Files:**
- Create: `src/test/java/analu/whereio/application/service/auth/LoginUserUsecaseImplTest.java`
- Modify: `src/main/java/analu/whereio/application/service/auth/LoginUserUsecaseImpl.java`

- [ ] **Step 1: Escrever os testes (vão falhar — `LoginAttemptService` ainda não está no usecase)**

Criar `src/test/java/analu/whereio/application/service/auth/LoginUserUsecaseImplTest.java`:

```java
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
```

- [ ] **Step 2: Executar os testes para confirmar falha**

```bash
./mvnw test -Dtest=LoginUserUsecaseImplTest
```

Esperado: testes de lockout falham — `LoginUserUsecaseImpl` ainda não integra o `LoginAttemptService`.

- [ ] **Step 3: Modificar `LoginUserUsecaseImpl.java`**

Substituir o conteúdo completo de `src/main/java/analu/whereio/application/service/auth/LoginUserUsecaseImpl.java`:

```java
package analu.whereio.application.service.auth;

import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.auth.LoginUserUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUserUsecaseImpl implements LoginUserUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuerService authTokenIssuerService;
    private final LoginAttemptService loginAttemptService;

    @Override
    public AuthTokens execute(String email, String rawPassword) {
        MDC.put("operation", "loginUser");
        try {
            String normalizedEmail = email.trim().toLowerCase();

            loginAttemptService.getLockoutRemaining(normalizedEmail).ifPresent(remaining -> {
                long minutes = remaining.toMinutes();
                long seconds = remaining.minusMinutes(minutes).toSeconds();
                throw new BusinessException(
                        String.format("Conta bloqueada. Tente novamente em %d min e %d seg", minutes, seconds),
                        HttpStatus.TOO_MANY_REQUESTS
                );
            });

            UserAccount user = userAccountRepositoryPort.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new BusinessException("Credenciais inválidas", HttpStatus.UNAUTHORIZED));

            if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getEncodedPassword())) {
                loginAttemptService.recordFailure(normalizedEmail);
                throw new BusinessException("Credenciais inválidas", HttpStatus.UNAUTHORIZED);
            }

            loginAttemptService.recordSuccess(normalizedEmail);
            return authTokenIssuerService.issueForUser(user);
        } finally {
            MDC.remove("operation");
        }
    }
}
```

- [ ] **Step 4: Executar todos os testes**

```bash
./mvnw test
```

Esperado: `BUILD SUCCESS`, toda a suíte passando.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/analu/whereio/application/service/auth/LoginUserUsecaseImpl.java \
        src/test/java/analu/whereio/application/service/auth/LoginUserUsecaseImplTest.java
git commit -m "feat: lock account for 10 min after 3 failed login attempts"
```
