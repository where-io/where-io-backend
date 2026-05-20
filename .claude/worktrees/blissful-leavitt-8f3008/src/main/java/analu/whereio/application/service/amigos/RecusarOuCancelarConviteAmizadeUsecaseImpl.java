package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.ports.in.amigos.RecusarOuCancelarConviteAmizadeUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecusarOuCancelarConviteAmizadeUsecaseImpl implements RecusarOuCancelarConviteAmizadeUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;

    @Override
    public void execute(String friendshipId, String currentUserId) {
        MDC.put("operation", "recusarOuCancelarConviteAmizade");
        try {
            Friendship friendship = friendshipRepositoryPort.findById(friendshipId)
                    .orElseThrow(() -> new BusinessException("Convite não encontrado", HttpStatus.NOT_FOUND));

            if (friendship.getStatus() != FriendshipStatus.PENDING) {
                throw new BusinessException("Somente convites pendentes podem ser cancelados ou recusados",
                        HttpStatus.BAD_REQUEST);
            }

            boolean participa = currentUserId.equals(friendship.getRequesterUserId())
                    || currentUserId.equals(friendship.getAddresseeUserId());
            if (!participa) {
                throw new BusinessException("Você não pode alterar este convite", HttpStatus.FORBIDDEN);
            }

            friendshipRepositoryPort.deleteById(friendshipId);
        } finally {
            MDC.remove("operation");
        }
    }
}
