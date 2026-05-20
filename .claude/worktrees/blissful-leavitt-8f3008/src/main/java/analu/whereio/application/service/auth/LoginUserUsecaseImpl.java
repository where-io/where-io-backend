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

import java.time.Duration;
import java.util.Optional;

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

            Optional<Duration> lockout = loginAttemptService.getLockoutRemaining(normalizedEmail);
            if (lockout.isPresent()) {
                Duration remaining = lockout.orElseThrow();
                long minutes = remaining.toMinutes();
                long seconds = remaining.minusMinutes(minutes).toSeconds();
                throw new BusinessException(
                        String.format("Conta bloqueada. Tente novamente em %d min e %d seg", minutes, seconds),
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }

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
