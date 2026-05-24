package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserMongoRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByNomeUsuario(String nomeUsuario);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNomeUsuario(String nomeUsuario);

    List<UserEntity> findByNomeUsuarioStartingWithIgnoreCase(String prefix, Pageable pageable);
}
