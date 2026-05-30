package analu.whereio.application.service.amigos;

import analu.whereio.application.model.Friendship;
import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.ports.in.amigos.AceitarConviteAmizadeUsecase;
import analu.whereio.application.ports.out.FriendshipRepositoryPort;
import analu.whereio.application.ports.out.SharingSettingsRepositoryPort;
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
    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;

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

            criarSharingSettingsDefault(salvo.getRequesterUserId(), salvo.getAddresseeUserId());
            criarSharingSettingsDefault(salvo.getAddresseeUserId(), salvo.getRequesterUserId());

            return salvo;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }

    private void criarSharingSettingsDefault(String fromUserId, String toUserId) {
        SharingSettings settings = new SharingSettings();
        settings.setFromUserId(fromUserId);
        settings.setToUserId(toUserId);
        settings.setShareLocation(true);
        settings.setSharePlaces(false);
        settings.setShareVisits(false);
        settings.setUpdatedAt(Instant.now());
        sharingSettingsRepositoryPort.save(settings);
    }
}
