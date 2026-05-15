package analu.whereio.application.ports.out;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    void persistRawToken(String rawRefreshToken, String userId, Instant expiresAt);

    Optional<String> findValidUserIdByRawToken(String rawRefreshToken);

    void revokeByRawToken(String rawRefreshToken);
}
