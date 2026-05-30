package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.mapper.SharingSettingsPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.SharingSettingsMongoRepository;
import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SharingSettingsRepositoryAdapter implements SharingSettingsRepositoryPort {

    private final SharingSettingsMongoRepository repository;
    private final SharingSettingsPersistenceMapper mapper;

    @Override
    public SharingSettings save(SharingSettings settings) {
        return mapper.toDomain(repository.save(mapper.toEntity(settings)));
    }

    @Override
    public Optional<SharingSettings> findByFromUserIdAndToUserId(String fromUserId, String toUserId) {
        return repository.findByFromUserIdAndToUserId(fromUserId, toUserId).map(mapper::toDomain);
    }

    @Override
    public List<SharingSettings> findByFromUserIdAndShareLocationTrue(String fromUserId) {
        return repository.findByFromUserIdAndShareLocationTrue(fromUserId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAllBetweenUsers(String userA, String userB) {
        repository.deleteAllBetweenUsers(userA, userB);
    }
}
