package analu.whereio.adapters.in.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendSharedLocalResponse {

    private LocalDtoResponse local;
    private String ownerFriendId;
    private String ownerFriendName;
}
