package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.UserEntity;
import analu.whereio.adapters.out.persistence.mapper.UserAccountPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.AppUserMongoRepository;
import analu.whereio.application.model.UserAccount;
import analu.whereio.application.ports.out.UserAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAccountRepositoryAdapter implements UserAccountRepositoryPort {

    private final AppUserMongoRepository repository;
    private final UserAccountPersistenceMapper mapper;

    @Override
    public UserAccount save(UserAccount user) {
        UserEntity saved = repository.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<UserAccount> findAllById(Collection<String> ids) {
        List<UserEntity> entities = new java.util.ArrayList<>();
        repository.findAllById(ids).forEach(entities::add);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<UserAccount> findByNome(String nomeUsuarioNormalizado) {
        return repository.findByNome(nomeUsuarioNormalizado).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }
}
