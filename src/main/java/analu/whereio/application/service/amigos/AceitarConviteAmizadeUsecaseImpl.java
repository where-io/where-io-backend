package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.ports.in.amigos.AceitarConviteAmizadeUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AceitarConviteAmizadeUsecaseImpl implements AceitarConviteAmizadeUsecase {

    private final FriendshipRepositoryPort friendshipRepositoryPort;

    @Override
    public Friendship execute(String friendshipId, String currentUserId) {
        MDC.put("operation", "aceitarConviteAmizade");
        try {
            Friendship friendship = friendshipRepositoryPort.findById(friendshipId)
                    .orElseThrow(() -> new BusinessException("Convite não encontrado", HttpStatus.NOT_FOUND));

            if (friendship.getStatus() != FriendshipStatus.PENDING) {
                throw new BusinessException("Este convite não está pendente", HttpStatus.BAD_REQUEST);
            }

            if (!friendship.getAddresseeUserId().equals(currentUserId)) {
                throw new BusinessException("Somente o destinatário pode aceitar o convite", HttpStatus.FORBIDDEN);
            }

            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendship.setAcceptedAt(Instant.now());

            Friendship salvo = friendshipRepositoryPort.save(friendship);
            MDC.put("entityId", salvo.getId());
            return salvo;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
