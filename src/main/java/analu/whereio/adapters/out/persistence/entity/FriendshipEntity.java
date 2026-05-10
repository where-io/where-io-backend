package analu.whereio.adapters.out.persistence.entity;

import analu.whereio.application.model.FriendshipStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "friendship_table")
@CompoundIndex(def = "{'requesterUserId': 1, 'addresseeUserId': 1}")
public class FriendshipEntity {

    @Id
    private String id;

    private String requesterUserId;
    private String addresseeUserId;
    private FriendshipStatus status;
    private Instant createdAt;
    private Instant acceptedAt;
}
