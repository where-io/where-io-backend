package analu.whereio.application.ports.in.amigos;

import analu.whereio.application.model.Friendship;

public interface AceitarConviteAmizadeUsecase {

    Friendship execute(String friendshipId, String currentUserId);
}
