package analu.whereio.application.ports.out;

import analu.whereio.application.model.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepositoryPort {

    Friendship save(Friendship friendship);

    Optional<Friendship> findById(String id);

    void deleteById(String id);

    List<Friendship> findPendingReceivedByAddressee(String addresseeUserId);

    List<Friendship> findPendingSentByRequester(String requesterUserId);

    List<Friendship> findAllBetweenUsers(String userIdA, String userIdB);

    List<Friendship> findAcceptedForUser(String userId);
}
