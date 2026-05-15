package analu.whereio.application.service.auth;

import analu.whereio.application.model.AuthTokens;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.JwtAuthenticationPort;
import analu.whereio.application.ports.out.RefreshTokenRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthTokenIssuerService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JwtAuthenticationPort jwtAuthenticationPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public AuthTokenIssuerService(
            JwtAuthenticationPort jwtAuthenticationPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.jwtAuthenticationPort = jwtAuthenticationPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public AuthTokens issueForUser(UserAccount user) {
        String accessToken = jwtAuthenticationPort.generateAccessToken(user);
        String refreshToken = newRawRefreshToken();
        Instant refreshExpiry = Instant.now().plusMillis(refreshExpirationMs);
        refreshTokenRepositoryPort.persistRawToken(refreshToken, user.getId(), refreshExpiry);
        long expiresInSeconds = Math.max(1L, accessExpirationMs / 1000);
        return new AuthTokens(accessToken, refreshToken, expiresInSeconds);
    }

    private static String newRawRefreshToken() {
        byte[] buf = new byte[48];
        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
