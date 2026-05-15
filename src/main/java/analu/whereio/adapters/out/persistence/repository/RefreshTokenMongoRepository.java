package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenMongoRepository extends MongoRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void deleteAllByUserId(String userId);
}
