package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.auth.RefreshTokenHasher;
import analu.whereio.adapters.out.persistence.entity.RefreshTokenEntity;
import analu.whereio.adapters.out.persistence.repository.RefreshTokenMongoRepository;
import analu.whereio.application.ports.out.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenMongoRepository repository;

    @Override
    public void persistRawToken(String rawRefreshToken, String userId, Instant expiresAt) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setUserId(userId);
        entity.setTokenHash(RefreshTokenHasher.sha256Hex(rawRefreshToken));
        entity.setExpiresAt(expiresAt);
        repository.save(entity);
    }

    @Override
    public Optional<String> findValidUserIdByRawToken(String rawRefreshToken) {
        String hash = RefreshTokenHasher.sha256Hex(rawRefreshToken);
        return repository.findByTokenHash(hash)
                .filter(e -> e.getExpiresAt() != null && e.getExpiresAt().isAfter(Instant.now()))
                .map(RefreshTokenEntity::getUserId);
    }

    @Override
    public void revokeByRawToken(String rawRefreshToken) {
        repository.deleteByTokenHash(RefreshTokenHasher.sha256Hex(rawRefreshToken));
    }
}
