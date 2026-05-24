package analu.whereio.application.service.auth;

import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.in.auth.RegisterUserUsecase;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import analu.whereio.application.util.NomeUsuarioNormalizer;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegisterUserUsecaseImpl implements RegisterUserUsecase {

    private final UserAccountRepositoryPort userAccountRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuerService authTokenIssuerService;

    @Override
    public AuthTokens execute(String email, String rawPassword, String nome, String nomeUsuarioRaw) {
        MDC.put("operation", "registerUser");
        try {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            if (userAccountRepositoryPort.existsByEmail(normalizedEmail)) {
                throw new BusinessException("E-mail já cadastrado", HttpStatus.CONFLICT);
            }
            if (rawPassword == null || rawPassword.length() < 8) {
                throw new BusinessException("Senha deve ter pelo menos 8 caracteres", HttpStatus.BAD_REQUEST);
            }

            String nomeUsuario = resolverNomeUsuario(nomeUsuarioRaw);

            UserAccount account = new UserAccount();
            account.setEmail(normalizedEmail);
            account.setEncodedPassword(passwordEncoder.encode(rawPassword));
            account.setNome(nome != null ? nome.trim() : "");
            account.setNomeUsuario(nomeUsuario);
            account.setCreatedAt(Instant.now());

            UserAccount saved = userAccountRepositoryPort.save(account);
            return authTokenIssuerService.issueForUser(saved);
        } finally {
            MDC.remove("operation");
        }
    }

    private String resolverNomeUsuario(String nomeUsuarioRaw) {
        if (nomeUsuarioRaw == null || nomeUsuarioRaw.isBlank()) {
            throw new BusinessException("nomeUsuario é obrigatório", HttpStatus.BAD_REQUEST);
        }
        String sanitized = NomeUsuarioNormalizer.sanitizePreferencia(nomeUsuarioRaw);
        if (sanitized.length() < 3) {
            throw new BusinessException("Nome de usuário inválido", HttpStatus.BAD_REQUEST);
        }
        if (userAccountRepositoryPort.existsByNomeUsuario(sanitized)) {
            throw new BusinessException("Nome de usuário já está em uso", HttpStatus.CONFLICT);
        }
        return sanitized;
    }
}
