package analu.whereio.adapters.out.persistence.impl;

import analu.whereio.adapters.out.persistence.entity.FriendshipEntity;
import analu.whereio.adapters.out.persistence.mapper.FriendshipPersistenceMapper;
import analu.whereio.adapters.out.persistence.repository.FriendshipMongoRepository;
import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FriendshipRepositoryAdapter implements FriendshipRepositoryPort {

    private final FriendshipMongoRepository repository;
    private final FriendshipPersistenceMapper mapper;

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipEntity saved = repository.save(mapper.toEntity(friendship));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Friendship> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Friendship> findPendingReceivedByAddressee(String addresseeUserId) {
        return repository.findByAddresseeUserIdAndStatus(addresseeUserId, FriendshipStatus.PENDING).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findPendingSentByRequester(String requesterUserId) {
        return repository.findByRequesterUserIdAndStatus(requesterUserId, FriendshipStatus.PENDING).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findAllBetweenUsers(String userIdA, String userIdB) {
        return repository.findAllBetweenUsers(userIdA, userIdB).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Friendship> findAcceptedForUser(String userId) {
        return repository.findByUserInvolvedAndStatus(userId, FriendshipStatus.ACCEPTED).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
