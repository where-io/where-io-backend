package analu.whereio.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Friendship {

    private String id;
    private String requesterUserId;
    private String addresseeUserId;
    private FriendshipStatus status;
    private Instant createdAt;
    private Instant acceptedAt;
}
