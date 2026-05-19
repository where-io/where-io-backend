package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.ports.in.amigos.RemoverAmigoUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoverAmigoUsecaseImpl implements RemoverAmigoUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;

    @Override
    public void execute(String friendshipId, String currentUserId) {
        MDC.put("operation", "removerAmigo");
        try {
            Friendship friendship = friendshipRepositoryPort.findById(friendshipId)
                    .orElseThrow(() -> new BusinessException("Amizade não encontrada", HttpStatus.NOT_FOUND));

            if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
                throw new BusinessException("Somente amizades aceitas podem ser removidas", HttpStatus.BAD_REQUEST);
            }

            boolean participa = currentUserId.equals(friendship.getRequesterUserId())
                    || currentUserId.equals(friendship.getAddresseeUserId());
            if (!participa) {
                throw new BusinessException("Você não faz parte desta amizade", HttpStatus.FORBIDDEN);
            }

            friendshipRepositoryPort.deleteById(friendshipId);
        } finally {
            MDC.remove("operation");
        }
    }
}
