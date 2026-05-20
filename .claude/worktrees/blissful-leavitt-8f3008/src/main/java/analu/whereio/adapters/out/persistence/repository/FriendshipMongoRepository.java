package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.FriendshipEntity;
import analu.whereio.application.model.FriendshipStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipMongoRepository extends MongoRepository<FriendshipEntity, String> {

    List<FriendshipEntity> findByAddresseeUserIdAndStatus(String addresseeUserId, FriendshipStatus status);

    List<FriendshipEntity> findByRequesterUserIdAndStatus(String requesterUserId, FriendshipStatus status);

    @Query("{ $or: [ { requesterUserId: ?0, addresseeUserId: ?1 }, { requesterUserId: ?1, addresseeUserId: ?0 } ] }")
    List<FriendshipEntity> findAllBetweenUsers(String userIdA, String userIdB);

    @Query("{ status: ?1, $or: [ { requesterUserId: ?0 }, { addresseeUserId: ?0 } ] }")
    List<FriendshipEntity> findByUserInvolvedAndStatus(String userId, FriendshipStatus status);
}
