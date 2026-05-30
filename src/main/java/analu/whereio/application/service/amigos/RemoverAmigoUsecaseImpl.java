package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.ports.in.amigos.RemoverAmigoUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoverAmigoUsecaseImpl implements RemoverAmigoUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;
    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;

    @Override
    public void execute(String amigoUserId, String currentUserId) {
        MDC.put("operation", "removerAmigo");
        try {
            Friendship friendship = friendshipRepositoryPort.findAllBetweenUsers(currentUserId, amigoUserId)
                    .stream()
                    .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Amizade não encontrada", HttpStatus.NOT_FOUND));

            friendshipRepositoryPort.deleteById(friendship.getId());
            sharingSettingsRepositoryPort.deleteAllBetweenUsers(currentUserId, amigoUserId);
        } finally {
            MDC.remove("operation");
        }
    }
}
