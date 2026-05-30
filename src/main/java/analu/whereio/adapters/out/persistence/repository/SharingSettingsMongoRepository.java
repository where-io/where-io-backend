package analu.whereio.adapters.out.persistence.repository;

import analu.whereio.adapters.out.persistence.entity.SharingSettingsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharingSettingsMongoRepository extends MongoRepository<SharingSettingsEntity, String> {

    Optional<SharingSettingsEntity> findByFromUserIdAndToUserId(String fromUserId, String toUserId);

    List<SharingSettingsEntity> findByFromUserIdAndShareLocationTrue(String fromUserId);

    void deleteByFromUserIdAndToUserId(String fromUserId, String toUserId);

    @Query(value = "{ $or: [ { fromUserId: ?0, toUserId: ?1 }, { fromUserId: ?1, toUserId: ?0 } ] }", delete = true)
    void deleteAllBetweenUsers(String userA, String userB);
}
