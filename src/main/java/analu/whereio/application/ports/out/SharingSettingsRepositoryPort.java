package analu.whereio.application.ports.out;

import analu.whereio.application.model.SharingSettings;

import java.util.List;
import java.util.Optional;

public interface SharingSettingsRepositoryPort {

    SharingSettings save(SharingSettings settings);

    Optional<SharingSettings> findByFromUserIdAndToUserId(String fromUserId, String toUserId);

    List<SharingSettings> findByFromUserIdAndShareLocationTrue(String fromUserId);

    void deleteAllBetweenUsers(String userA, String userB);
}
