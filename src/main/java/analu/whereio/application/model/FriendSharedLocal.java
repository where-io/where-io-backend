package analu.whereio.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendSharedLocal {

    private final Local local;
    private final String ownerFriendId;
    private final String ownerFriendName;
}
