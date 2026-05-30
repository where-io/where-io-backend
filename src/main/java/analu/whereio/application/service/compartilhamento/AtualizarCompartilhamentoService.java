package analu.whereio.application.service.compartilhamento;

import analu.whereio.application.model.FriendshipStatus;
import analu.whereio.application.model.SharingSettings;
import analu.whereio.application.ports.in.compartilhamento.AtualizarCompartilhamentoUsecase;
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
public class AtualizarCompartilhamentoService implements AtualizarCompartilhamentoUsecase {

    private final SharingSettingsRepositoryPort sharingSettingsRepositoryPort;
    private final FriendshipRepositoryPort friendshipRepositoryPort;

    @Override
    public SharingSettings execute(String requesterId, String friendId, boolean shareLocation, boolean sharePlaces, boolean shareVisits) {
        MDC.put("operation", "atualizarCompartilhamento");
        try {
            boolean saoAmigos = friendshipRepositoryPort.findAllBetweenUsers(requesterId, friendId)
                    .stream()
                    .anyMatch(f -> f.getStatus() == FriendshipStatus.ACCEPTED);

            if (!saoAmigos) {
                throw new BusinessException("Você não é amigo deste usuário", HttpStatus.FORBIDDEN);
            }

            SharingSettings settings = sharingSettingsRepositoryPort
                    .findByFromUserIdAndToUserId(requesterId, friendId)
                    .orElseGet(() -> {
                        SharingSettings novo = new SharingSettings();
                        novo.setFromUserId(requesterId);
                        novo.setToUserId(friendId);
                        return novo;
                    });

            settings.setShareLocation(shareLocation);
            settings.setSharePlaces(sharePlaces);
            settings.setShareVisits(shareVisits);
            settings.setUpdatedAt(Instant.now());

            SharingSettings salvo = sharingSettingsRepositoryPort.save(settings);
            MDC.put("entityId", salvo.getId());
            return salvo;
        } finally {
            MDC.remove("operation");
            MDC.remove("entityId");
        }
    }
}
